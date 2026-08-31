package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.*;
import com.fooddelivery.delivery.event.DeliveryStatusEvent;
import com.fooddelivery.delivery.exception.BadRequestException;
import com.fooddelivery.delivery.exception.ResourceNotFoundException;
import com.fooddelivery.delivery.exception.UnauthorizedException;
import com.fooddelivery.delivery.kafka.DeliveryEventProducer;
import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.model.DeliveryTask;
import com.fooddelivery.delivery.model.PartnerStatus;
import com.fooddelivery.delivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.delivery.repository.DeliveryTaskRepository;
import com.fooddelivery.delivery.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryTaskService {

    private final DeliveryTaskRepository taskRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryEventProducer eventProducer;

    @Transactional
    public DeliveryTaskResponse createDeliveryTask(CreateDeliveryTaskRequest request) {
        if (taskRepository.existsByOrderId(request.getOrderId())) {
            throw new BadRequestException("Delivery task already exists for order ID: " + request.getOrderId());
        }

        DeliveryTask task = DeliveryTask.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .customerId(request.getCustomerId())
                .customerPhone(request.getCustomerPhone())
                .restaurantId(request.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryFee(request.getDeliveryFee())
                .notes(request.getNotes())
                .status(DeliveryStatus.PENDING)
                .build();

        DeliveryTask savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    public List<DeliveryTaskResponse> getAvailableDeliveries() {
        List<DeliveryTask> tasks = taskRepository.findByStatus(DeliveryStatus.PENDING);
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public DeliveryTaskResponse acceptDelivery(Long taskId, UserPrincipal currentUser) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("You must create a delivery partner profile first"));

        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found with ID: " + taskId));

        if (task.getStatus() != DeliveryStatus.PENDING) {
            throw new BadRequestException("Delivery is already assigned or not available. Status: " + task.getStatus());
        }

        task.setPartner(partner);
        task.setStatus(DeliveryStatus.ACCEPTED);
        task.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));

        partner.setStatus(PartnerStatus.BUSY);
        partnerRepository.save(partner);

        DeliveryTask updatedTask = taskRepository.save(task);

        // Publish event
        publishStatusEvent(updatedTask, "Delivery partner accepted the task");

        return mapToResponse(updatedTask);
    }

    @Transactional
    public DeliveryTaskResponse updateDeliveryStatus(Long taskId, UpdateDeliveryStatusRequest request, UserPrincipal currentUser) {
        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found with ID: " + taskId));

        DeliveryPartner partner = task.getPartner();
        boolean isAdmin = currentUser.hasRole("ROLE_ADMIN");

        if (!isAdmin && (partner == null || !partner.getUserId().equals(currentUser.getId()))) {
            throw new UnauthorizedException("You are not authorized to update this delivery task");
        }

        DeliveryStatus newStatus = request.getStatus();
        task.setStatus(newStatus);
        if (request.getNotes() != null) {
            task.setNotes(request.getNotes());
        }

        LocalDateTime now = LocalDateTime.now();
        if (newStatus == DeliveryStatus.PICKED_UP || newStatus == DeliveryStatus.OUT_FOR_DELIVERY) {
            if (task.getPickedUpAt() == null) {
                task.setPickedUpAt(now);
            }
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            task.setDeliveredAt(now);
            if (partner != null) {
                partner.setStatus(PartnerStatus.AVAILABLE);
                partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
                partnerRepository.save(partner);
            }
        } else if (newStatus == DeliveryStatus.CANCELLED || newStatus == DeliveryStatus.FAILED) {
            if (partner != null) {
                partner.setStatus(PartnerStatus.AVAILABLE);
                partnerRepository.save(partner);
            }
        }

        DeliveryTask updatedTask = taskRepository.save(task);

        // Publish event
        publishStatusEvent(updatedTask, request.getNotes() != null ? request.getNotes() : "Status updated to " + newStatus);

        return mapToResponse(updatedTask);
    }

    public DeliveryTaskResponse getDeliveryById(Long taskId) {
        DeliveryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found with ID: " + taskId));
        return mapToResponse(task);
    }

    public DeliveryTrackingResponse getDeliveryTrackingByOrderId(Long orderId) {
        DeliveryTask task = taskRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery tracking not found for order ID: " + orderId));

        DeliveryPartner partner = task.getPartner();

        return DeliveryTrackingResponse.builder()
                .orderId(task.getOrderId())
                .orderNumber(task.getOrderNumber())
                .deliveryStatus(task.getStatus())
                .partnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null ? partner.getFullName() : null)
                .partnerPhone(partner != null ? partner.getPhone() : null)
                .vehicleType(partner != null ? partner.getVehicleType() : null)
                .vehicleNumber(partner != null ? partner.getVehicleNumber() : null)
                .partnerLatitude(partner != null ? partner.getCurrentLatitude() : null)
                .partnerLongitude(partner != null ? partner.getCurrentLongitude() : null)
                .estimatedDeliveryTime(task.getEstimatedDeliveryTime())
                .pickedUpAt(task.getPickedUpAt())
                .deliveredAt(task.getDeliveredAt())
                .build();
    }

    public List<DeliveryTaskResponse> getPartnerDeliveries(UserPrincipal currentUser, DeliveryStatus status) {
        DeliveryPartner partner = partnerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Delivery partner profile not found"));

        List<DeliveryTask> tasks;
        if (status != null) {
            tasks = taskRepository.findByPartnerAndStatus(partner, status);
        } else {
            tasks = taskRepository.findByPartnerOrderByCreatedAtDesc(partner);
        }
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<DeliveryTaskResponse> getAllDeliveries(DeliveryStatus status) {
        List<DeliveryTask> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void publishStatusEvent(DeliveryTask task, String notes) {
        try {
            DeliveryPartner partner = task.getPartner();
            DeliveryStatusEvent event = DeliveryStatusEvent.builder()
                    .deliveryId(task.getId())
                    .orderId(task.getOrderId())
                    .orderNumber(task.getOrderNumber())
                    .partnerId(partner != null ? partner.getId() : null)
                    .partnerName(partner != null ? partner.getFullName() : null)
                    .partnerPhone(partner != null ? partner.getPhone() : null)
                    .status(task.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .notes(notes)
                    .build();
            eventProducer.sendDeliveryStatusEvent(event);
        } catch (Exception e) {
            // Log but do not block transaction
        }
    }

    public DeliveryTaskResponse mapToResponse(DeliveryTask task) {
        DeliveryPartner partner = task.getPartner();
        return DeliveryTaskResponse.builder()
                .id(task.getId())
                .orderId(task.getOrderId())
                .orderNumber(task.getOrderNumber())
                .customerId(task.getCustomerId())
                .customerPhone(task.getCustomerPhone())
                .restaurantId(task.getRestaurantId())
                .restaurantName(task.getRestaurantName())
                .pickupAddress(task.getPickupAddress())
                .deliveryAddress(task.getDeliveryAddress())
                .partnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null ? partner.getFullName() : null)
                .partnerPhone(partner != null ? partner.getPhone() : null)
                .status(task.getStatus())
                .deliveryFee(task.getDeliveryFee())
                .estimatedDeliveryTime(task.getEstimatedDeliveryTime())
                .pickedUpAt(task.getPickedUpAt())
                .deliveredAt(task.getDeliveredAt())
                .notes(task.getNotes())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
