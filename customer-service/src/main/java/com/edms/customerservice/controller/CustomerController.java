package com.edms.customerservice.controller;


import com.edms.customerservice.dto.CustomerRequestDTO;
import com.edms.customerservice.dto.CustomerResponseDTO;
import com.edms.customerservice.dto.validators.CreateCustomerValidationGroup;
import com.edms.customerservice.service.CustomerService;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getCustomers() {
        final List<CustomerResponseDTO> customers = customerService.getCustomers();
        return ResponseEntity.ok().body(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Validated({Default.class, CreateCustomerValidationGroup.class}) @RequestBody CustomerRequestDTO customerRequestDTO) {
        final CustomerResponseDTO newCustomer = customerService.createCustomer(customerRequestDTO);
        return ResponseEntity.ok().body(newCustomer);
    }

    @PostMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable UUID id, @Validated({Default.class}) @RequestBody CustomerRequestDTO customerRequestDTO) {
        final CustomerResponseDTO customerResponseDTO = customerService.updateCustomer(id, customerRequestDTO);
        return ResponseEntity.ok().body(customerResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().body("User with id {" + id + "} has been successfully removed");
    }
}
