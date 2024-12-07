package com.example.hotelmobile.model;

import java.util.Date;

public class Booking {
    private String hotelName;
    private String roomName;
    private String invoiceCode;
    private Date startDate;
    private Date endDate;
    private double totalPrice;
    private String paymentStatus;
    private String userId;
    public Booking(){

    }
    public Booking(String hotelName, String roomName, String invoiceCode, Date startDate, Date endDate, double totalPrice, String paymentStatus, String userId) {
        this.hotelName = hotelName;
        this.roomName = roomName;
        this.invoiceCode = invoiceCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.userId = userId;
    }

    // Getters and Setters
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
