package com.fetch.receiptprocessorchallenge.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptPointResponse {
    private Integer points;
    private String errorMessage;
}
