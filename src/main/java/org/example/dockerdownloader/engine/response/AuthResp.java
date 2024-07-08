package org.example.dockerdownloader.engine.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <pre>
 * {
 *     "token": "jwt",
 *     "access_token": "jwt",
 *     "expires_in": 300,
 *     "issued_at": "2024-07-02T01:48:34.84391488Z"
 * }
 * </pre>
 *
 * @author XSJ
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuthResp extends ErrorResp {

    @JsonProperty("token")
    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("issued_at")
    private String issuedAt;
}
