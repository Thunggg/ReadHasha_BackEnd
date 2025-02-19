package com.example.thuan.ultis;

public enum Role {
    ROLE_ADMIN(0),
    ROLE_CUSTOMER(1),
    ROLE_SELLER_STAFF(2),
    ROLE_WAREHOUSE_STAFF(3);

    private int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
