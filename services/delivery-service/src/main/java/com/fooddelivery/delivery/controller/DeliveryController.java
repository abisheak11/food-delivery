package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.*;
import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.security.UserPrincipal;
import com.fooddelivery.delivery.service.DeliveryTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "APIs for delivery task creation, assignment, tracking, and status progression")
public class DeliveryController {

    private final DeliveryTaskService taskService;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "delivery-service"));
    }

    @PostMapping("/tasks")
    @Operation(summary = "Create a new delivery task")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT')")
    public ResponseEntity<DeliveryTaskResponse> createDeliveryTask(
            @Valid @RequestBody CreateDeliveryTaskRequest request
    ) {
        DeliveryTaskResponse response = taskService.createDeliveryTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    @Operation(summary = "Get all unassigned deliveries available for pickup")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryTaskResponse>> getAvailableDeliveries() {
        List<DeliveryTaskResponse> response = taskService.getAvailableDeliveries();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Delivery partner accepts a delivery task")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryTaskResponse> acceptDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryTaskResponse response = taskService.acceptDelivery(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update delivery status (e.g. PICKED_UP, DELIVERED, CANCELLED)")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryTaskResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryTaskResponse response = taskService.updateDeliveryStatus(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery task details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryTaskResponse> getDeliveryById(@PathVariable Long id) {
        DeliveryTaskResponse response = taskService.getDeliveryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Track delivery details and partner location for a specific order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryTrackingResponse> getTrackingByOrderId(@PathVariable Long orderId) {
        DeliveryTrackingResponse response = taskService.getDeliveryTrackingByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/partner/me")
    @Operation(summary = "Get current partner's active and historical delivery tasks")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryTaskResponse>> getPartnerDeliveries(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) DeliveryStatus status
    ) {
        List<DeliveryTaskResponse> response = taskService.getPartnerDeliveries(currentUser, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all delivery tasks (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryTaskResponse>> getAllDeliveries(
            @RequestParam(required = false) DeliveryStatus status
    ) {
        List<DeliveryTaskResponse> response = taskService.getAllDeliveries(status);
        return ResponseEntity.ok(response);
    }
}
