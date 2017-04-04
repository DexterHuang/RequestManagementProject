package com.dexter.requestmanagement.Models;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;


public class Request {
    private String hotelName;
    private String requestCreator;
    private String buildingNumber;
    private String roomNumber;
    private ArrayList<String> items;
    private RequestStatusType status;
    private String photoUrl;
    private String id;

    public Request() {

    }

    public Request(String ID, String buildingNumber, String roomNumber, ArrayList<String> items) {
        this.id = ID;
        this.buildingNumber = buildingNumber;
        this.roomNumber = roomNumber;
        this.items = items;
        this.status = RequestStatusType.PENDING;
        this.requestCreator = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getRequestCreator() {
        return requestCreator;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public RequestStatusType getStatus() {
        if (status == null) {
            status = RequestStatusType.PENDING;
        }
        return status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getID() {
        return id;
    }

}
