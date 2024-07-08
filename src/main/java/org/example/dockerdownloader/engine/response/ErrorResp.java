package org.example.dockerdownloader.engine.response;

import lombok.Data;

import java.util.Collection;

/**
 * @author XSJ
 * @version 1.0.0
 */
@Data
public class ErrorResp {

    private Collection<Error> errors;

    @Data
    public static class Error {
        private String code;
        private String message;
        private String detail;
    }
}
