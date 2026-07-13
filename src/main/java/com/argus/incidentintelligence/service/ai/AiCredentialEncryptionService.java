package com.argus.incidentintelligence.service.ai;

import com.argus.exception.ApiException;
import com.argus.incidentintelligence.config.IncidentIntelligenceProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiCredentialEncryptionService {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final IncidentIntelligenceProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AiCredentialEncryptionService(IncidentIntelligenceProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String plaintext) {
        requireSecret();
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array());
        } catch (GeneralSecurityException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to encrypt AI provider credentials");
        }
    }

    public String decrypt(String encryptedValue) {
        requireSecret();
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedValue);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to decrypt AI provider credentials");
        }
    }

    private SecretKeySpec key() throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(properties.ai().encryptionSecret().getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, "AES");
    }

    private void requireSecret() {
        if (!StringUtils.hasText(properties.ai().encryptionSecret())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "AI credential encryption secret is not configured"
            );
        }
    }
}
