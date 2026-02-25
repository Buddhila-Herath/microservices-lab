package com.example.orderservice;

public class Order {
    private int id;
    private String item;
    private int quantity;
    private String customerId;

    public Order() {}

    public Order(int id, String item, int quantity, String customerId) {
        this.id = id;
        this.item = item;
        this.quantity = quantity;
        this.customerId = customerId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
