package com.arduino.shop_api.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message){
        super(message);
    }
}