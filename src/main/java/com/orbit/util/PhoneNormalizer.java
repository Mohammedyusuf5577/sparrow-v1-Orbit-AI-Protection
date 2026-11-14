package com.orbit.util;

import java.util.regex.Pattern;

public final class PhoneNormalizer {

    // E.164 standard: + followed by 7 to 15 digits
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    private PhoneNormalizer() {
    }

    public static String normalize(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String cleaned = rawPhone.trim().replaceAll("[\\s\\-\\(\\)]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        if (!cleaned.startsWith("+")) {
            // If standard 10 digit number without country code, default to India (+91)
            if (cleaned.matches("^[6-9]\\d{9}$")) {
                cleaned = "+91" + cleaned;
            } else {
                cleaned = "+" + cleaned;
            }
        }
        return E164_PATTERN.matcher(cleaned).matches() ? cleaned : null;
    }

    public static boolean isValid(String phone) {
        return phone != null && E164_PATTERN.matcher(phone).matches();
    }
}
