package com.penny.expense.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CsvUploadResult {
    private int added;
    private int failed;
    private List<String> errors;
}
