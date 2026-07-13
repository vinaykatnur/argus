package com.argus.service;

import com.argus.exception.ApiException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MonitorUrlNormalizer {

    public String normalize(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor URL is required");
        }

        try {
            URI uri = new URI(rawUrl.trim()).normalize();
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor URL must use HTTP or HTTPS");
            }
            if (!StringUtils.hasText(uri.getHost())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor URL must include a valid host");
            }
            if (StringUtils.hasText(uri.getUserInfo())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor URL must not include user credentials");
            }

            URI normalized = new URI(
                    scheme.toLowerCase(Locale.ROOT),
                    null,
                    uri.getHost().toLowerCase(Locale.ROOT),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    null
            );
            return normalized.toASCIIString();
        } catch (URISyntaxException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor URL is malformed");
        }
    }
}
