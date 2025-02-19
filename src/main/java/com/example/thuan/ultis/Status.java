package com.example.thuan.ultis;

public enum Status {
    INACTIVE_STATUS(0),
    ACTIVE_STATUS(1),
    UNVERIFIED_STATUS(2),
    UNVERIFIED_ADMIN_CREATED_STATUS(4);

    private int value;

    Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
