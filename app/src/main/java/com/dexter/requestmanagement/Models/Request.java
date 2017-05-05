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
    private ArrayList<String> initialPhotoUrls = new ArrayList<>();
    private ArrayList<String> checkInPhotoUrls = new ArrayList<>();
    private ArrayList<String> completionPhotoUrls = new ArrayList<>();
    private String id;
    private String description;

    private ArrayList<String> assignedAgents = new ArrayList<String>();

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
        if (items == null) {
            items = new ArrayList<String>();
        }
        return items;
    }

    public RequestStatusType getStatus() {
        if (status == null) {
            status = RequestStatusType.PENDING;
        }
        return status;
    }

    public String getID() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description + "";
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    @Override
    public String toString() {
        return this.hotelName + " " + this.buildingNumber + " " + this.roomNumber;
    }


    public ArrayList<String> getAssignedAgents() {
        if (assignedAgents == null) {
            assignedAgents = new ArrayList<>();
        }
        return assignedAgents;
    }

    public void setStatus(RequestStatusType status) {
        this.status = status;
    }

    public ArrayList<String> getInitialPhotoUrls() {
        return initialPhotoUrls;
    }

    public void setInitialPhotoUrls(ArrayList<String> initialPhotoUrls) {
        this.initialPhotoUrls = initialPhotoUrls;
    }

    public ArrayList<String> getCheckInPhotoUrls() {
        return checkInPhotoUrls;
    }

    public void setCheckInPhotoUrls(ArrayList<String> checkInPhotoUrls) {
        this.checkInPhotoUrls = checkInPhotoUrls;
    }

    public ArrayList<String> getCompletionPhotoUrls() {
        return completionPhotoUrls;
    }

    public void setCompletionPhotoUrls(ArrayList<String> completionPhotoUrls) {
        this.completionPhotoUrls = completionPhotoUrls;
    }
}
