package com.orbit.payout.util;

import com.orbit.exception.AppException;
import com.orbit.util.AppUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    @Value("${app.encryption.secret}")
    private String secretKeyString;

    private SecretKeySpec getSecretKey() {
        if (secretKeyString == null || secretKeyString.length() < 16) {
            throw new AppException(AppUtils.Status.UNEXPECTED_ERROR, "Encryption secret is invalid or too short");
        }
        // Use first 16 bytes for 128-bit AES OR 32 bytes for 256-bit AES
        byte[] key = secretKeyString.substring(0, Math.min(secretKeyString.length(), 32)).getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(key, ALGORITHM);
    }

    public String encrypt(String valueToEnc) {
        if (valueToEnc == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encValue = cipher.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encValue);
        } catch (Exception e) {
            throw new AppException(AppUtils.Status.UNEXPECTED_ERROR, "Error encrypting data");
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decValue = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(decValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AppException(AppUtils.Status.UNEXPECTED_ERROR, "Error decrypting data");
        }
    }
}
