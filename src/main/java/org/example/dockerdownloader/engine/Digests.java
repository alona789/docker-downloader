package org.example.dockerdownloader.engine;

import lombok.Data;

@Data
public class Digests {

    private String digest;

    private String algorithm;

    private String value;

    public Digests(String value) {
        this.value = value;
        if (value.contains(":")) {
            String[] split = value.split(":");
            this.algorithm = split[0];
            this.digest = split[1];
        }
    }
}