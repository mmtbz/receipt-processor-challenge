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

@Service
public class ReceiptService {
    private final ReceiptDAO _receiptDao;

    @Autowired
    ReceiptService(ReceiptDAO receiptDAO){
        _receiptDao = receiptDAO;
    }

    /**
     * Take the receipt request transform it into the Receipt Object and store it to the db
     * Generate a new UUID for the receipt to mimic DB operation
     * @param request ProcessReceiptRequest
     * @return ProcessReceiptResponse response object for the processed receipt
     * */
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

    public ReceiptPointResponse getReceiptPoints(String receiptId){
        Optional<Receipt> receiptOptional = _receiptDao.findById(receiptId);
        if(receiptOptional.isEmpty()) {
            throw new RuntimeException(String.format("Receipt with Id %s is not found.", receiptId));
        }
        return ReceiptPointResponse.builder()
                .points(calculatePoints(receiptOptional.get())).build();
    }

    /**
     * Helper Method to calculate receipt points
     * @param receipt receipt to calculate point for
     * @return int points
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
     * Helper method to count alphaNumeric character in a string
     * @param name input sting
     * @return int count of alphaNumeric characters
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
