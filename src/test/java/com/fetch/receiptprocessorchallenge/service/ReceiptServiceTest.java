package com.fetch.receiptprocessorchallenge.service;

import com.fetch.receiptprocessorchallenge.dao.ReceiptDAO;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptRequest;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptResponse;
import com.fetch.receiptprocessorchallenge.dtos.ReceiptPointResponse;
import com.fetch.receiptprocessorchallenge.models.Item;
import com.fetch.receiptprocessorchallenge.models.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptDAO receiptDAO;

    @InjectMocks
    private ReceiptService receiptService;

    private ProcessReceiptRequest request;
    private Receipt receipt;
    private String receiptId;

    @BeforeEach
    void setUp() {
        receiptId = UUID.randomUUID().toString();
        request = new ProcessReceiptRequest();
        request.setRetailer("TestStore");
        request.setPurchaseDate(LocalDate.of(2022, 3, 20));
        request.setPurchaseTime(LocalTime.of(14, 33));
        request.setItems(List.of(new Item("Item1", 10.0), new Item("Item2", 15.0)));
        request.setTotal(25.0);

        receipt = Receipt.builder()
                .id(receiptId)
                .retailer(request.getRetailer())
                .purchaseDate(request.getPurchaseDate())
                .purchaseTime(request.getPurchaseTime())
                .items(request.getItems())
                .total(request.getTotal())
                .build();
    }

    @Test
    void testProcessReceipt() {
        when(receiptDAO.save(any(Receipt.class))).thenReturn(receipt);

        ProcessReceiptResponse response = receiptService.processReceipt(request);

        assertNotNull(response);
        assertEquals(receiptId, response.getId());
        verify(receiptDAO, times(1)).save(any(Receipt.class));
    }

    @Test
    void testGetReceiptPoints_Success() {
        when(receiptDAO.findById(receiptId)).thenReturn(Optional.of(receipt));

        ReceiptPointResponse response = receiptService.getReceiptPoints(receiptId);

        assertNotNull(response);
        assertTrue(response.getPoints() > 0);
        verify(receiptDAO, times(1)).findById(receiptId);
    }

    @Test
    void testGetReceiptPoints_ReceiptNotFound() {
        when(receiptDAO.findById(receiptId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            receiptService.getReceiptPoints(receiptId);
        });

        assertEquals("Receipt with Id " + receiptId + " is not found.", exception.getMessage());
        verify(receiptDAO, times(1)).findById(receiptId);
    }
}
