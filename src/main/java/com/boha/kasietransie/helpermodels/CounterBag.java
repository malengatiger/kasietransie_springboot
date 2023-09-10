package com.boha.kasietransie.helpermodels;

import lombok.Data;

@Data
public class CounterBag {
    long count;
    String description;

    public CounterBag(long count, String description) {
        this.count = count;
        this.description = description;
    }
}
