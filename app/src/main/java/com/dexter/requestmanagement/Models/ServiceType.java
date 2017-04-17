package com.dexter.requestmanagement.Models;

public class ServiceType {
    private String serviceName;
    private float price;
    private String description;

    public ServiceType(){

    }
    public ServiceType(String name, float price){
        this.serviceName = name;
        this.price = price;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceName() {

        return serviceName;
    }

    public float getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
