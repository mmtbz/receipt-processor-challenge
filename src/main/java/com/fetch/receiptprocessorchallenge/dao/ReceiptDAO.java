package com.fetch.receiptprocessorchallenge.dao;

import com.fetch.receiptprocessorchallenge.models.Receipt;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository class to deal with Receipt CRUD operations
 * Uses a hashMap as an in-memory db.
 * */
@Repository
public class ReceiptDAO {
    private final Map<String, Receipt> receipts = new HashMap<>();

    /**
     * Saves the receipt to the DB
     * @param receipt to be saved
     * @return receipt
     * */
    public Receipt save(Receipt receipt) {
        receipts.put(receipt.getId(), receipt);
        return receipts.get(receipt.getId());
    }

    /**
     * Find the receipt by given id
     * @param id receiptId
     * @return optional receipt
     * */
    public Optional<Receipt> findById(String id) {
        return Optional.ofNullable(receipts.get(id));
    }
}
