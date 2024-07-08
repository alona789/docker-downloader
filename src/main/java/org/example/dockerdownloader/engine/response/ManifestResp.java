package org.example.dockerdownloader.engine.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Map;

/**
 * 清单描述
 *
 * @author XSJ
 * @version 1.0.0
 * @see <a href="https://github.com/opencontainers/image-spec/blob/v1.0.1/manifest.md#image-manifest">镜像清单描述</a>
 * @see <a href="https://github.com/distribution/distribution/blob/main/docs/content/spec/manifest-v2-2.md">镜像清单描述2.2</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ManifestResp extends ErrorResp {

    private int schemaVersion;
    private String mediaType;
    private Collection<Manifests> manifests;
    private Config config;
    private Collection<Layers> layers;


    @Data
    public static class Manifests {
        private Map<String, String> annotations;
        private String digest;
        private String mediaType;
        private Platform platform;
        private int size;

        @Data
        public static class Platform {
            private String architecture;
            private String os;
        }
    }

    @Data
    public static class Config {

        private String mediaType;
        private int size;
        private String digest;
    }

    @Data
    public static class Layers {

        private String mediaType;
        private long size;
        private String digest;
    }
}
