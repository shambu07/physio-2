package com.clinic.physioclinic.service.ex;

public class SlotAlreadyTakenException extends RuntimeException {
    public SlotAlreadyTakenException(String msg) {
        super(msg);
    }
}
