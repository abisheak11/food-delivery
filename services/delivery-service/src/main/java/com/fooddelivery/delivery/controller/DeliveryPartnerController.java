package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.DeliveryPartnerProfileRequest;
import com.fooddelivery.delivery.dto.DeliveryPartnerResponse;
import com.fooddelivery.delivery.dto.UpdateAvailabilityRequest;
import com.fooddelivery.delivery.dto.UpdateLocationRequest;
import com.fooddelivery.delivery.model.PartnerStatus;
import com.fooddelivery.delivery.security.UserPrincipal;
import com.fooddelivery.delivery.service.DeliveryPartnerService;
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

@RestController
@RequestMapping("/api/deliveries/partners")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner Management", description = "APIs for delivery partner profile, availability, and location tracking")
public class DeliveryPartnerController {

    private final DeliveryPartnerService partnerService;

    @PostMapping("/profile")
    @Operation(summary = "Create or update delivery partner profile")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponse> createOrUpdateProfile(
            @Valid @RequestBody DeliveryPartnerProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryPartnerResponse response = partnerService.createOrUpdateProfile(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current delivery partner's profile")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponse> getCurrentProfile(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryPartnerResponse response = partnerService.getCurrentPartnerProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/availability")
    @Operation(summary = "Update delivery partner online/offline availability")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponse> updateAvailability(
            @Valid @RequestBody UpdateAvailabilityRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryPartnerResponse response = partnerService.updateAvailability(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/location")
    @Operation(summary = "Update real-time GPS location of delivery partner")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartnerResponse> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        DeliveryPartnerResponse response = partnerService.updateLocation(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all delivery partners (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryPartnerResponse>> getAllPartners(
            @RequestParam(required = false) PartnerStatus status
    ) {
        List<DeliveryPartnerResponse> response = partnerService.getAllPartners(status);
        return ResponseEntity.ok(response);
    }
}
