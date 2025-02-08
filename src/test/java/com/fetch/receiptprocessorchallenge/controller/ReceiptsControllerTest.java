package com.fetch.receiptprocessorchallenge.controller;

import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptRequest;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptResponse;
import com.fetch.receiptprocessorchallenge.dtos.ReceiptPointResponse;
import com.fetch.receiptprocessorchallenge.models.Item;
import com.fetch.receiptprocessorchallenge.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;

/**
 * Unit test class for ReceiptsController.class
 * */
class ReceiptsControllerTest {
    private ReceiptsController receiptsController;
    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        receiptService = Mockito.mock(ReceiptService.class);
        receiptsController = new ReceiptsController(receiptService);
    }

    @Test
    void shouldThrowErrorWhenNoReceiptId() {
        ResponseEntity<ReceiptPointResponse> response = receiptsController.getReceiptPoints(null);
        assertThat("Id should be present.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowErrorWhenProvidedInvalidUUID() {
        ResponseEntity<ReceiptPointResponse> response = receiptsController.getReceiptPoints("id-invalid-uuid");
        assertThat("The provided id is not a valid uuid.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenRetailerIsMissing() {
        ProcessReceiptRequest request = new ProcessReceiptRequest();
        request.setPurchaseDate(LocalDate.parse("2022-03-20"));
        request.setPurchaseTime(LocalTime.parse("14:33"));
        request.setItems(Collections.singletonList(new Item("Item 1", 10.0)));
        request.setTotal(10.0);

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Retailer name is required.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenPurchaseDateIsInvalid() {
        ProcessReceiptRequest request = createValidRequest();
        request.setPurchaseDate(null);

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Purchase date is required.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenPurchaseTimeIsInvalid() {
        ProcessReceiptRequest request = createValidRequest();
        request.setPurchaseTime(null);

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Purchase time is required.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenItemsAreEmpty() {
        ProcessReceiptRequest request = createValidRequest();
        request.setItems(Collections.emptyList());

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("At least one item is required.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenItemHasNoDescription() {
        ProcessReceiptRequest request = createValidRequest();
        request.getItems().get(0).setShortDescription("");

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Item description is required.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenItemPriceIsNegative() {
        ProcessReceiptRequest request = createValidRequest();
        request.getItems().get(0).setPrice(-1.0);

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Item price must be greater than 0.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenTotalAmountDoesNotMatchItemsSum() {
        ProcessReceiptRequest request = createValidRequest();
        request.setTotal(5.00); // Incorrect total

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);
        assertThat("Total amount does not match the sum of item prices.").isEqualTo(response.getBody().getErrorMessage());
    }

    @Test
    void shouldPassValidationForValidRequest() {
        ProcessReceiptRequest request = createValidRequest();
        assertThatNoException().isThrownBy(() -> receiptsController.processReceipt(request));
    }

    @Test
    void shouldProcessReceiptSuccessfully() {
        ProcessReceiptRequest request = createValidRequest();
        Mockito.when(receiptService.processReceipt(any())).thenReturn(ProcessReceiptResponse.builder()
                .id(UUID.randomUUID().toString()).build());

        ResponseEntity<ProcessReceiptResponse> response = receiptsController.processReceipt(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotBlank();
    }

    private ProcessReceiptRequest createValidRequest() {
        ProcessReceiptRequest request = new ProcessReceiptRequest();
        request.setRetailer("BestBuy");
        request.setPurchaseDate(LocalDate.parse("2022-03-20"));
        request.setPurchaseTime(LocalTime.parse("14:33"));
        request.setItems(Arrays.asList(
                new Item("Laptop", 500.0),
                new Item("Mouse", 20.0)
        ));
        request.setTotal(520.0);
        return request;
    }
}
