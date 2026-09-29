package com.eda_project.tickets.controller;

import lombok.Data;

@Data
public class PurchaseRequest {

    private Long eventId;
    private String customerName;
    private String customerEmail;
}
