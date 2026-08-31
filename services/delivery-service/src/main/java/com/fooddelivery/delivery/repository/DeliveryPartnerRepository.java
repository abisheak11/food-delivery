package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.model.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUserId(Long userId);
    List<DeliveryPartner> findByStatus(PartnerStatus status);
    boolean existsByUserId(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);
}
