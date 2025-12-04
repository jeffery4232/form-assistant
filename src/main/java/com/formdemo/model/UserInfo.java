package com.formdemo.model;

import java.util.List;

public class UserInfo {
    private String name;
    private String phone;
    private String email;
    private String idCard;
    private List<String> preferredTransportation;
    private String defaultCity;

    public UserInfo() {
    }

    public UserInfo(String name, String phone, String email, String idCard, 
                   List<String> preferredTransportation, String defaultCity) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.idCard = idCard;
        this.preferredTransportation = preferredTransportation;
        this.defaultCity = defaultCity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public List<String> getPreferredTransportation() {
        return preferredTransportation;
    }

    public void setPreferredTransportation(List<String> preferredTransportation) {
        this.preferredTransportation = preferredTransportation;
    }

    public String getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(String defaultCity) {
        this.defaultCity = defaultCity;
    }
}
