package com.androidcollider.easyfin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Rates {

    private int id;
    private long date;
    private String currency;
    private String rateType;
    private double bid;
    private double ask;
}


