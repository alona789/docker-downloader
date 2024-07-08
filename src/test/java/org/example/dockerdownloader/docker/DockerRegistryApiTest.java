package org.example.dockerdownloader.docker;

import com.dtflys.forest.config.ForestConfiguration;
import com.dtflys.forest.logging.DefaultLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.IOUtils;
import org.example.dockerdownloader.engine.Digests;
import org.example.dockerdownloader.constans.DockerConstants;
import org.example.dockerdownloader.engine.ManifestFileDTO;
import org.example.dockerdownloader.engine.DockerAuthApi;
import org.example.dockerdownloader.engine.DockerRegistryApi;
import org.example.dockerdownloader.engine.response.AuthResp;
import org.example.dockerdownloader.engine.response.ManifestResp;
import org.example.dockerdownloader.util.JsonUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static com.dtflys.forest.Forest.client;
import static com.dtflys.forest.Forest.config;
import static org.example.dockerdownloader.constans.AcceptConstants.*;
import static org.example.dockerdownloader.constans.DockerConstants.DOCKER_REGISTRY_URL;
import static org.example.dockerdownloader.util.TarUtil.createTarGzipFolder;
import static org.example.dockerdownloader.util.TarUtil.decompressGzipFile;

class DockerRegistryApiTest {

    private static final Logger log = LoggerFactory.getLogger(DockerRegistryApiTest.class);
    private static String imageName = "library/nginx";
    private static String tag = "latest";

    private DockerRegistryApi registryApi = client(DockerRegistryApi.class);

    @BeforeAll
    static void beforeAll() {
        ForestConfiguration config = config();
        config.setLogRequest(true);
        config.setLogEnabled(true);
        config.setLogHandler(new DefaultLogHandler());
        config.setLogResponseStatus(true);
        config.setLogResponseContent(true);
        config.setConnectTimeout(30 * 1000);
        config.setReadTimeout(30 * 1000);
    }

    @Test
    void auth() {
        DockerAuthApi api = client(DockerAuthApi.class);
        AuthResp resp = api.auth(DockerConstants.DOCKER_AUTH_URL, "registry.docker.io", "library/ubuntu");
        System.out.println(resp);
    }

    @Test
    void manifestByTag() {
        ManifestResp resp = registryApi.manifest(DOCKER_REGISTRY_URL, imageName, "latest", VND_OCI_IMAGE_MANIFEST_V1_JSON);
        System.out.println(resp);
    }

    @Test
    void manifestByDigest() {
        ManifestResp resp = registryApi.manifest(DOCKER_REGISTRY_URL, imageName, "sha256:c920ba4cfca05503764b785c16b76d43c83a6df8d1ab107e7e6610000d94315c", VND_OCI_IMAGE_INDEX_V1_JSON);
        System.out.println(resp);
    }

    ObjectMapper mapper = new ObjectMapper();


//    @Test
    void pull() throws IOException, ArchiveException {

        Path tempDirectory = Files.createTempDirectory("docker_pull_cache_");
        log.info("tempDirectory: {}", tempDirectory);

        ManifestResp tagManifests = registryApi.manifest(DOCKER_REGISTRY_URL, imageName, tag, VND_DOCKER_DISTRIBUTION_MANIFEST_V2_JSON);
        log.debug("resp: {}", tagManifests);

        ManifestResp.Manifests manifests = tagManifests.getManifests().toArray(new ManifestResp.Manifests[0])[0];
        // getFsLayers
        ManifestResp manifest = registryApi.manifest(DOCKER_REGISTRY_URL, imageName, manifests.getDigest(), VND_OCI_IMAGE_MANIFEST_V1_JSON);

        // getDockerConfig
        // https://github.com/opencontainers/image-spec/blob/v1.0.1/config.md
        ManifestResp.Config config = manifest.getConfig();
        InputStream configInputStream = registryApi.blobs(DOCKER_REGISTRY_URL, imageName, config.getDigest());
//        InputStream configInputStream = registryApi.downloadManifest(DOCKER_REGISTRY_URL, imageName, config.getDigest(), config.getMediaType());
        // 把configBytes 写入磁盘
        Digests digests = new Digests(config.getDigest());
        String configJsonFileName = String.format("%s.json", digests.getDigest());
//        Files.write(tempDirectory.resolve(configJsonFileName), configBytes);
        Files.copy(configInputStream, tempDirectory.resolve(configJsonFileName));
        IOUtils.close(configInputStream);

        ManifestFileDTO manifestFileDTO = new ManifestFileDTO();

        for (ManifestResp.Layers layer : manifest.getLayers()) {

            // 创建layer文件夹
            String digest = layer.getDigest();
            Path layerDirPath = tempDirectory.resolve(new Digests(digest).getDigest());
            File layerDir = layerDirPath.toFile();
            layerDir.mkdirs();

            // 创建VERSION 文件
            Files.write(layerDirPath.resolve("VERSION"), "1.0".getBytes());

            // 下载layer.tar
            InputStream layerInputSteam = registryApi.blobs(DOCKER_REGISTRY_URL, imageName, digest);
//            Path layerTarGzPath = layerDirPath.resolve("layer.tar.gz");
//            Files.copy(layerInputSteam, layerTarGzPath);
//            IOUtils.close(layerInputSteam);

            // gz 解压
            Path layerTarPath = layerDirPath.resolve("layer.tar");
//            decompressGzipFile(layerTarGzPath.toString(), layerTarPath.toString());
            decompressGzipFile(layerInputSteam, layerTarPath.toString());
            IOUtils.close(layerInputSteam);

            String layerItem = layerTarPath.toString().replace(tempDirectory.toString() + File.separatorChar, "")
                    .replace(File.separatorChar, '/');

//            manifestFileDTO.getLayers().add(String.format("%s%s%s", File.separatorChar, new Digests(digest).getDigest(), "layer.tar"));
            manifestFileDTO.getLayers().add(layerItem);
        }

        // 最后写入
        manifestFileDTO.getRepoTags().add(String.format("%s:%s", imageName, tag));
        manifestFileDTO.setConfig(configJsonFileName);

        Files.write(tempDirectory.resolve("manifest.json"), JsonUtil.toJson(List.of(manifestFileDTO)).getBytes());

//        Path imageFile = Files.createTempFile("docker_pull_cache_", ArchiveStreamFactory.TAR);

//        // ArchiveStreamFactory 把tempDirectory打成tar包
//        ArchiveInputStream<? extends ArchiveEntry> input = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, Files.newInputStream(tempDirectory));
//        IOUtils.copy(input, Files.newOutputStream(imageFile));
//        IOUtils.close(input);


        createTarGzipFolder(tempDirectory, Paths.get(tempDirectory.getFileName()+".tar"));
    }

}