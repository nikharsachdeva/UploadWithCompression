package com.nikharsachdeva.uploadcompression;

import com.google.gson.annotations.SerializedName;

public class RatingModel {

    @SerializedName("response")
    private String response;

    @SerializedName("name")
    private String name;

    @SerializedName("text")
    private String text;

    @SerializedName("number")
    private String number;

    public RatingModel(String text, String number, String name, String response) {

        this.response = response;
        this.text = text;
        this.number = number;
        this.name = name;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
