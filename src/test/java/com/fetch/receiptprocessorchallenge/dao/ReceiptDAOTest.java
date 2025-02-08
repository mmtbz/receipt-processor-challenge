package com.fetch.receiptprocessorchallenge.dao;

import com.fetch.receiptprocessorchallenge.models.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit test class for ReceiptDAO.class
 * */
class ReceiptDAOTest {

    private ReceiptDAO receiptDAO;
    private Receipt receipt;
    private String receiptId;

    @BeforeEach
    void setUp() {
        receiptDAO = new ReceiptDAO();
        receiptId = UUID.randomUUID().toString();
        receipt = Receipt.builder()
                .id(receiptId)
                .retailer("TestStore")
                .purchaseDate(LocalDate.of(2022, 3, 20))
                .purchaseTime(LocalTime.of(14, 33))
                .items(List.of())
                .total(25.0)
                .build();
    }

    @Test
    void testSaveReceipt() {
        Receipt savedReceipt = receiptDAO.save(receipt);
        assertNotNull(savedReceipt);
        assertEquals(receiptId, savedReceipt.getId());
    }

    @Test
    void testFindById_Found() {
        receiptDAO.save(receipt);
        Optional<Receipt> foundReceipt = receiptDAO.findById(receiptId);
        assertTrue(foundReceipt.isPresent());
        assertEquals(receiptId, foundReceipt.get().getId());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Receipt> foundReceipt = receiptDAO.findById(UUID.randomUUID().toString());
        assertFalse(foundReceipt.isPresent());
    }
}
