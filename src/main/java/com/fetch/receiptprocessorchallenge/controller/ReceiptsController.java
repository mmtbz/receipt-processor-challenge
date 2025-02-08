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

/**
 * Controller class for handling Receipt API endpoints.
 * Exposes endpoints under the `/receipts` path
 * for processing and retrieving receipt data.
 */
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
     * GET endpoint to calculate and retrieve the reward points
     * for a given receipt ID.
     *
     * @param id The receipt ID, which must be a valid UUID string.
     * @return The calculated receipt points.
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
     * Validates the `getReceiptPoint` request using the following logic:
     * 1. Ensures the receipt ID is present.
     * 2. Verifies that the provided ID is a valid UUID format.
     *
     * The receipt ID follows the UUID format for uniqueness and consistency.
     *
     * @param id The receipt ID to validate.
     * @throws RuntimeException if the ID is missing or not a valid UUID.
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
     * POST endpoint to process and store a receipt in the database.
     *
     * @param request The receipt data to be processed and saved.
     * @return The processed receipt with a unique identifier.
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
     * Validates the `processReceipt` request based on the following criteria:
     *
     * 1. Ensures the retailer name is provided and valid.
     * 2. Validates the purchase date format (yyyy-MM-dd).
     * 3. Validates the purchase time format (HH:mm).
     * 4. Ensures at least one item is present in the receipt.
     * 5. Validates that each item has a description and a price greater than zero.
     * 6. Ensures the total amount is greater than zero.
     * 7. Verifies that the total amount matches the sum of all item prices.
     *
     * @param request The receipt object to validate.
     * @throws RuntimeException if any validation rule is violated.
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
