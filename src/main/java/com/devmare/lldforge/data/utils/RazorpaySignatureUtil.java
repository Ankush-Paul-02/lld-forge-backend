package com.devmare.lldforge.data.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RazorpaySignatureUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";

    public static boolean verifySignature(String payload, String razorpaySignature, String secret) {
        try {
            Mac sha256HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);
            sha256HMAC.init(secretKey);

            byte[] hash = sha256HMAC.doFinal(payload.getBytes());
            String generatedSignature = Base64.getEncoder().encodeToString(hash);

            return generatedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            return false;
        }
    }
}
