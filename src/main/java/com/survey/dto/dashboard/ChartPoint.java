package com.survey.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartPoint {
    private String label;
    private int value;
}
