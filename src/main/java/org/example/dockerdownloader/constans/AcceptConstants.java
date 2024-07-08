package org.example.dockerdownloader.constans;

/**
 * @author XSJ
 * @version 1.0.0
 * @see <a href="https://distribution.github.io/distribution/spec/manifest-v2-2/">Manifest Media Types</a>
 */
public interface AcceptConstants {

    String VND_OCI_IMAGE_LAYER_V1_TAR_GZIP = "application/vnd.oci.image.layer.v1.tar+gzip";
    String VND_DOCKER_DISTRIBUTION_MANIFEST_V2_JSON = "application/vnd.docker.distribution.manifest.v2+json";

    String VND_OCI_IMAGE_INDEX_V1_JSON = "application/vnd.oci.image.index.v1+json";
    String VND_OCI_IMAGE_MANIFEST_V1_JSON = "application/vnd.oci.image.manifest.v1+json";
}
