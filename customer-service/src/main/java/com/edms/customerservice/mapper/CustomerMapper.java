package com.edms.customerservice.mapper;

import com.edms.customerservice.dto.CustomerRequestDTO;
import com.edms.customerservice.dto.CustomerResponseDTO;
import com.edms.customerservice.model.Customer;

import java.time.LocalDate;

public class CustomerMapper {
    public static CustomerResponseDTO toDTO(Customer customer) {
        final CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();

        customerResponseDTO.setId(customer.getId().toString());
        customerResponseDTO.setName(customer.getName());
        customerResponseDTO.setEmail(customer.getEmail());
        customerResponseDTO.setAddress(customer.getAddress());
        customerResponseDTO.setDateOfBirth(customer.getDateOfBirth().toString());

        return customerResponseDTO;
    }

    public static Customer toModel(CustomerRequestDTO customerRequestDTO) {
        final Customer customer = new Customer();

        customer.setName(customerRequestDTO.getName());
        customer.setEmail(customerRequestDTO.getEmail());
        customer.setAddress(customerRequestDTO.getAddress());
        customer.setDateOfBirth(LocalDate.parse(customerRequestDTO.getDateOfBirth()));
        customer.setRegisteredDate(LocalDate.parse(customerRequestDTO.getRegisteredDate()));

        return customer;
    }
}
