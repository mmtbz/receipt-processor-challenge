package com.fetch.receiptprocessorchallenge.service;

import com.fetch.receiptprocessorchallenge.dao.ReceiptDAO;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptRequest;
import com.fetch.receiptprocessorchallenge.dtos.ProcessReceiptResponse;
import com.fetch.receiptprocessorchallenge.dtos.ReceiptPointResponse;
import com.fetch.receiptprocessorchallenge.models.Item;
import com.fetch.receiptprocessorchallenge.models.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for handling business logic related to receipts.
 * This class processes receipt data and calculates reward points
 * based on predefined rules.
 */
@Service
public class ReceiptService {
    private final ReceiptDAO _receiptDao;

    @Autowired
    ReceiptService(ReceiptDAO receiptDAO){
        _receiptDao = receiptDAO;
    }

    /**
     * Processes the receipt request by transforming it into a `Receipt` object
     * and storing it in the database.
     *
     * A new UUID is generated for the receipt to simulate database behavior.
     *
     * @param request The `ProcessReceiptRequest` containing receipt details.
     * @return `ProcessReceiptResponse` containing the stored receipt's details.
     */
    public ProcessReceiptResponse processReceipt(ProcessReceiptRequest request){
        Receipt newReceipt = Receipt.builder()
                .items(request.getItems())
                .retailer(request.getRetailer())
                .purchaseDate(request.getPurchaseDate())
                .purchaseTime(request.getPurchaseTime())
                .total(request.getTotal())
                .id(UUID.randomUUID().toString())
                .build();
        Receipt savedReceipt = _receiptDao.save(newReceipt);
        return ProcessReceiptResponse.builder()
                .id(savedReceipt.getId()).build();
    }

    /**
     * Calculates the points for a given receipt.
     *
     * @param receiptId the ID of the receipt to calculate points for
     * @return the calculated points for the receipt
     * @throws RuntimeException if the receipt with the specified ID is not found
     */
    public ReceiptPointResponse getReceiptPoints(String receiptId){
        Optional<Receipt> receiptOptional = _receiptDao.findById(receiptId);
        if(receiptOptional.isEmpty()) {
            throw new RuntimeException(String.format("Receipt with Id %s is not found.", receiptId));
        }
        return ReceiptPointResponse.builder()
                .points(calculatePoints(receiptOptional.get())).build();
    }

    /**
     * Helper method to calculate reward points for a given receipt.
     *
     * The calculation is based on predefined rules,
     * such as retailer name length, total amount, item count,
     * and purchase date/time conditions.
     *
     * @param receipt The receipt for which points are to be calculated.
     * @return The total points awarded for the receipt.
     */
    private int calculatePoints(Receipt receipt) {
        int points = 0;

        //One point for every alphanumeric character in the retailer name.
        points += countAlphaNumeric(receipt.getRetailer());

        // 50 points if the total is a round dollar amount with no cents.
        if (receipt.getTotal() % 1 == 0) points += 50;

        // 25 points if the total is a multiple of 0.25.
        if (receipt.getTotal() % 0.25 == 0) points += 25;

        // 5 points for every two items on the receipt.
        points += (receipt.getItems().size() / 2) * 5;

        // If the trimmed length of the item description is a multiple of 3,
        // multiply the price by 0.2 and round up to the nearest integer.
        // The result is the number of points earned.
        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                points += Math.ceil(item.getPrice() * 0.2);
            }
        }

        // 6 points if the day in the purchase date is odd.
        if (receipt.getPurchaseDate().getDayOfMonth() % 2 != 0) points += 6;

        // 10 points if the time of purchase is after 2:00pm and before 4:00pm.
        LocalTime time = receipt.getPurchaseTime();
        LocalTime fixedStart = LocalTime.of(14, 0, 0).atOffset(ZoneOffset.UTC).toLocalTime();
        LocalTime fixedEnd = LocalTime.of(16, 0, 0).atOffset(ZoneOffset.UTC).toLocalTime();
        if (receipt.getPurchaseTime().isAfter(fixedStart) && time.isBefore(fixedEnd)) points += 10;

        return points;
    }

    /**
     * Helper method to count the number of alphanumeric characters in a given string.
     *
     * Alphanumeric characters include letters (A-Z, a-z) and digits (0-9).
     *
     * @param name The input string to evaluate.
     * @return The count of alphanumeric characters in the string.
     */
    private int countAlphaNumeric(String name){
        int response = 0;
        for(Character c: name.toCharArray()) {
            if(Character.isLetterOrDigit(c)){
                response++;
            }
        }
        return response;
    }
}
