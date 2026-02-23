package com.edms.customerservice.service;

import com.edms.customerservice.dto.CustomerRequestDTO;
import com.edms.customerservice.dto.CustomerResponseDTO;
import com.edms.customerservice.exception.CustomerNotFoundException;
import com.edms.customerservice.exception.EmailAlreadyExistsException;
import com.edms.customerservice.grpc.BillingServiceGrpcClient;
import com.edms.customerservice.kafka.KafkaProducer;
import com.edms.customerservice.mapper.CustomerMapper;
import com.edms.customerservice.model.Customer;
import com.edms.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public CustomerService(CustomerRepository customerRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.customerRepository = customerRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<CustomerResponseDTO> getCustomers() {
        final List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(CustomerMapper::toDTO)
                .toList();
    }

    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO) {
        if (customerRepository.existsByEmail(customerRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists " + customerRequestDTO.getEmail());
        }

        Customer newCustomer = customerRepository.save(CustomerMapper.toModel(customerRequestDTO));

        billingServiceGrpcClient.createBillingAccount(
                newCustomer.getId().toString(),
                newCustomer.getName(),
                newCustomer.getEmail()
        );

        kafkaProducer.sendEvent(newCustomer);

        return CustomerMapper.toDTO(newCustomer);
    }

    public CustomerResponseDTO updateCustomer(UUID customerToUpdateId, CustomerRequestDTO customerRequestDTO) {
        Customer customerToUpdate = customerRepository.findById(customerToUpdateId).orElseThrow(() -> new CustomerNotFoundException("Customer not found with id " + customerToUpdateId));

        if (customerRepository.existsByEmailAndIdNot(customerRequestDTO.getEmail(), customerToUpdateId)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists " + customerRequestDTO.getEmail());
        }

        customerToUpdate.setName(customerRequestDTO.getName());
        customerToUpdate.setEmail(customerRequestDTO.getEmail());
        customerToUpdate.setAddress(customerRequestDTO.getAddress());
        customerToUpdate.setDateOfBirth(LocalDate.parse(customerRequestDTO.getDateOfBirth()));

        Customer updatedCustomer = customerRepository.save(customerToUpdate);
        return CustomerMapper.toDTO(updatedCustomer);
    }

    public void deleteCustomer(UUID customerToDeleteId) {
        customerRepository.deleteById(customerToDeleteId);
    }
}
