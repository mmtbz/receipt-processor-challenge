package com.fetch.receiptprocessorchallenge.controller;

import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptRequest;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptResponse;
import com.fetch.receiptprocessorchallenge.dtos.ReceiptPointResponse;
import com.fetch.receiptprocessorchallenge.models.Item;
import com.fetch.receiptprocessorchallenge.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequestMapping("/receipts")
public class ReceiptsController {
    private final ReceiptService _receiptService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    ReceiptsController(ReceiptService receiptService) {
        _receiptService = receiptService;
    }

    /**
     * Endpoint to calculate the receipt point for the given receipt id
     * @param id the receipt id, It should be a valid UUID string
     * @return Receipt point
     */
    @GetMapping("/{id}/points")
    public ResponseEntity<ReceiptPointResponse> getReceiptPoints(@PathVariable String id) {
        try {
            validateGetReceiptPointRequest(id);
            return ResponseEntity.ok(_receiptService.getReceiptPoints(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ReceiptPointResponse.builder()
                    .errorMessage(e.getMessage()).build());
        }
    }

    /**
     * Validate getReceiptPoint request with bellow logic
     * 1. Check if the id is present
     * 2. validate that the string id is a valid UUID. I use UUID for the ID of the receipt.
     */
    private void validateGetReceiptPointRequest(String id) {
        if (id == null) {
            throw new RuntimeException("Id should be present.");
        }
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("The provided id is not a valid uuid.");
        }
    }

    /**
     * Endpoint to process the receipt
     * @param request to be processed
     * @return receipt
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessReceiptResponse> processReceipt(@RequestBody ProcessReceiptRequest request) {
        try {
            validateProcessReceiptRequest(request);
            return ResponseEntity.ok(_receiptService.processReceipt(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ProcessReceiptResponse.builder()
                    .errorMessage(e.getMessage()).build());
        }
    }

    /**
     * Validate processReceipt request with bellow logic
     * 1. validate retailer name
     * 2. validate purchase date and should be in format yyyy-MM-dd
     * 3. validate purchase time and should be in format HH:mm
     * 4. validate items exists and at least one item should be present
     * 5. validate each item contains description and the price is greater than 0
     * 6. validate the total amount to be greater than zero
     * 7. validate the total amount equals the total of all items
     */
    private void validateProcessReceiptRequest(ProcessReceiptRequest request) {
        if (request.getRetailer() == null || request.getRetailer().isEmpty()) {
            throw new IllegalArgumentException("Retailer name is required.");
        }

        if (request.getPurchaseDate() == null) {
            throw new IllegalArgumentException("Purchase date is required.");
        }
        try {
            LocalDate.parse(request.getPurchaseDate().toString(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Purchase date must be in the format yyyy-MM-dd");
        }

        if (request.getPurchaseTime() == null) {
            throw new IllegalArgumentException("Purchase time is required.");
        }
        try {
            LocalTime.parse(request.getPurchaseTime().toString(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Purchase time must be in the format HH:mm");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required.");
        }

        double calculatedTotal = 0.0;

        for (Item item : request.getItems()) {
            if (item.getShortDescription() == null || item.getShortDescription().isEmpty()) {
                throw new IllegalArgumentException("Item description is required.");
            }
            if (item.getPrice() == null || item.getPrice() < 0) {
                throw new IllegalArgumentException("Item price must be greater than 0.");
            }
            calculatedTotal += item.getPrice();
        }

        if (request.getTotal() == null || request.getTotal() < 0) {
            throw new IllegalArgumentException("Total amount must be greater than 0.");
        }

        if (Math.abs(calculatedTotal - request.getTotal()) != 0.00) {
            throw new IllegalArgumentException("Total amount does not match the sum of item prices.");
        }
    }
}
