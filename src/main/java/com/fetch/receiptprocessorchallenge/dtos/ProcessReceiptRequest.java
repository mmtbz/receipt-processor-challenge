package com.fetch.receiptprocessorchallenge.dtos;

import com.fetch.receiptprocessorchallenge.models.Item;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ProcessReceiptRequest {
    private String retailer;
    private LocalDate purchaseDate;
    private LocalTime purchaseTime;
    private List<Item> items;
    private Double total;
}
