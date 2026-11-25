package com.hbooking.api;

import com.hbooking.dto.request.CreateOwnerRequest;
import com.hbooking.model.Owner;
import com.hbooking.exception.EntityNotFoundException;
import com.hbooking.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owners")
public class OwnerController {
    private final PropertyService propertyService;

    public OwnerController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<Owner> registerOwner(@RequestBody CreateOwnerRequest request) {
        Owner owner = propertyService.registerOwner(request.getName(), request.getEmail(), request.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(owner);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Owner> getOwner(@PathVariable String id) {
        Owner owner = propertyService.getOwner(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with ID: " + id));
        return ResponseEntity.ok(owner);
    }
}
