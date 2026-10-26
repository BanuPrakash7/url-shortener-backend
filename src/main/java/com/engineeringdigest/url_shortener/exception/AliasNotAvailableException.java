package com.engineeringdigest.url_shortener.exception;

public class AliasNotAvailableException extends RuntimeException{

    public AliasNotAvailableException(String message){
        super(message);
    }

}
