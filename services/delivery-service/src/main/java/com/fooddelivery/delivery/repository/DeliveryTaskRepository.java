package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.model.DeliveryTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    Optional<DeliveryTask> findByOrderId(Long orderId);
    Optional<DeliveryTask> findByOrderNumber(String orderNumber);
    List<DeliveryTask> findByStatus(DeliveryStatus status);
    List<DeliveryTask> findByPartnerOrderByCreatedAtDesc(DeliveryPartner partner);
    List<DeliveryTask> findByPartnerAndStatus(DeliveryPartner partner, DeliveryStatus status);
    List<DeliveryTask> findByPartnerAndStatusIn(DeliveryPartner partner, List<DeliveryStatus> statuses);
    boolean existsByOrderId(Long orderId);
}
