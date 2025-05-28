package com.csci334.EventHub.utils;

import java.security.SecureRandom;

public class IdGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int ID_BOUND = 10000000; // Upper bound for random IDs

    public static Integer generateRandomId() {
        return random.nextInt(ID_BOUND);
    }
}