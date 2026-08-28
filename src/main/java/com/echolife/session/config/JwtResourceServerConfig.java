package com.echolife.session.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Configuration
public class JwtResourceServerConfig {

    @Value("${echolife.jwt.public-key-path:./keys/public.pem}")
    private String publicKeyPath;

    @Value("${echolife.jwt.issuer:http://identity-consent-service}")
    private String issuer;

    @PostConstruct
    public void printIssuer() {

        System.out.println("========================================");
        System.out.println("JWT ISSUER CONFIGURED = [" + issuer + "]");
        System.out.println("JWT ISSUER LENGTH      = " + issuer.length());

        System.out.println(
                "JWT ISSUER BYTES       = " +
                        Arrays.toString(
                                issuer.getBytes(StandardCharsets.UTF_8)
                        )
        );

        System.out.println("JWT PUBLIC KEY PATH   = " + publicKeyPath);
        System.out.println("========================================");
    }

    @Bean
    public RSAPublicKey sessionPublicKey() throws Exception {

        String key = Files.readString(Path.of(publicKeyPath))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(key);

        return (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(
                        new X509EncodedKeySpec(decodedKey)
                );
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey sessionPublicKey) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(sessionPublicKey)
                        .build();

        OAuth2TokenValidator<Jwt> validator = jwt -> {

            System.out.println();
            System.out.println("========================================");
            System.out.println("JWT VALIDATION STARTED");
            System.out.println("========================================");

            /*
             * Spring Security 7 returns the issuer as URL.
             */
            URL tokenIssuerUrl = jwt.getIssuer();

            String tokenIssuer =
                    tokenIssuerUrl == null
                            ? null
                            : tokenIssuerUrl.toString();

            System.out.println(
                    "EXPECTED ISSUER = [" + issuer + "]"
            );

            System.out.println(
                    "TOKEN ISSUER    = [" + tokenIssuer + "]"
            );

            System.out.println(
                    "EXPECTED LENGTH = " + issuer.length()
            );

            System.out.println(
                    "TOKEN LENGTH    = " +
                            (tokenIssuer == null
                                    ? "null"
                                    : tokenIssuer.length())
            );

            if (tokenIssuer != null) {

                System.out.println(
                        "TOKEN ISSUER BYTES = " +
                                Arrays.toString(
                                        tokenIssuer.getBytes(
                                                StandardCharsets.UTF_8
                                        )
                                )
                );
            }

            /*
             * ISSUER VALIDATION
             */
            if (tokenIssuer == null ||
                    !issuer.equals(tokenIssuer)) {

                System.out.println(
                        "❌ JWT ISSUER VALIDATION FAILED"
                );

                OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "Invalid issuer. Expected: ["
                                + issuer
                                + "], but received: ["
                                + tokenIssuer
                                + "]",
                        null
                );

                return OAuth2TokenValidatorResult.failure(error);
            }

            System.out.println(
                    "✅ JWT ISSUER VALIDATION PASSED"
            );

            /*
             * AUDIENCE VALIDATION
             */
            System.out.println(
                    "TOKEN AUDIENCE = " + jwt.getAudience()
            );

            if (jwt.getAudience() == null ||
                    !jwt.getAudience()
                            .contains("echolife-session")) {

                System.out.println(
                        "❌ JWT AUDIENCE VALIDATION FAILED"
                );

                OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "Invalid audience. Expected: echolife-session",
                        null
                );

                return OAuth2TokenValidatorResult.failure(error);
            }

            System.out.println(
                    "✅ JWT AUDIENCE VALIDATION PASSED"
            );

            /*
             * MFA INFORMATION
             *
             * We are only printing MFA.
             * We are NOT rejecting the request based on MFA.
             */
            Object mfa = jwt.getClaims().get("mfa");

            System.out.println(
                    "JWT MFA CLAIM = " + mfa
            );

            /*
             * SUBJECT
             */
            System.out.println(
                    "JWT SUBJECT = " + jwt.getSubject()
            );

            /*
             * ROLE
             */
            Object role = jwt.getClaims().get("role");

            System.out.println(
                    "JWT ROLE = " + role
            );

            System.out.println("========================================");
            System.out.println("✅ JWT VALIDATION PASSED");
            System.out.println("========================================");

            return OAuth2TokenValidatorResult.success();
        };

        decoder.setJwtValidator(validator);

        return decoder;
    }
}