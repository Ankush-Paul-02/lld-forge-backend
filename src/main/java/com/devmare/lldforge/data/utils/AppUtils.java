package com.devmare.lldforge.data.utils;

import java.util.regex.Pattern;

public class AppUtils {
    /**
     * Validates the email format using regex.
     *
     * @param email The email to validate.
     * @return true if the email is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
