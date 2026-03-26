package com.Payment.Shop.exception;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter

public class ErrorDetails {
    private Date timestamp;
    private String message;
    private String details;
//    private ErrorCode errorCode;

    public ErrorDetails(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }


}
