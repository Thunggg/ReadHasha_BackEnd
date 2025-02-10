package com.example.thuan.ultis;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class RandomNumberGenerator {

    public String generateNumber() {
        Random random = new Random();
        return String.valueOf(100_000 + random.nextInt(900_000));
    }
}
