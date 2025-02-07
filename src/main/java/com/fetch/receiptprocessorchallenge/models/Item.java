package com.fetch.receiptprocessorchallenge.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Item {
    private String shortDescription;
    private Double price;
}
