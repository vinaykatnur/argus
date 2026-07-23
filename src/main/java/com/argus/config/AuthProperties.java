package com.argus.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "argus.auth")
public class AuthProperties {

    @Min(5)
    private long verificationTokenExpirationMinutes;

    @Min(5)
    private long passwordResetTokenExpirationMinutes;

    private boolean requireEmailVerification = false;

    @NotBlank
    private String frontendBaseUrl;

    public long getVerificationTokenExpirationMinutes() {
        return verificationTokenExpirationMinutes;
    }

    public void setVerificationTokenExpirationMinutes(long verificationTokenExpirationMinutes) {
        this.verificationTokenExpirationMinutes = verificationTokenExpirationMinutes;
    }

    public long getPasswordResetTokenExpirationMinutes() {
        return passwordResetTokenExpirationMinutes;
    }

    public void setPasswordResetTokenExpirationMinutes(long passwordResetTokenExpirationMinutes) {
        this.passwordResetTokenExpirationMinutes = passwordResetTokenExpirationMinutes;
    }

    public boolean isRequireEmailVerification() {
        return requireEmailVerification;
    }

    public void setRequireEmailVerification(boolean requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }
}
