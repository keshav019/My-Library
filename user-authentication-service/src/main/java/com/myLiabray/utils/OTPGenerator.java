package com.myLiabray.utils;

import java.util.Random;

public class OTPGenerator {
    public static String generateOTP() {
        String characters = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            char digit = characters.charAt(index);
            otp.append(digit);
        }
        return otp.toString();
    }
}
