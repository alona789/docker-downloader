package org.example.dockerdownloader.engine;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.example.dockerdownloader.dto.PullImageDTO;
import org.example.dockerdownloader.engine.response.ManifestResp;
import org.example.dockerdownloader.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static com.dtflys.forest.Forest.client;
import static org.example.dockerdownloader.constans.AcceptConstants.VND_DOCKER_DISTRIBUTION_MANIFEST_V2_JSON;
import static org.example.dockerdownloader.constans.AcceptConstants.VND_OCI_IMAGE_MANIFEST_V1_JSON;
import static org.example.dockerdownloader.util.TarUtil.createTarGzipFolder;
import static org.example.dockerdownloader.util.TarUtil.decompressGzipFile;

/**
 * @author XSJ
 * @version 1.0.0
 */
@Slf4j
public class DockerClient {

    private static DockerRegistryApi registryApi = client(DockerRegistryApi.class);

    public static Path pullImage(PullImageDTO pullImageDTO) throws IOException {
        String registry = pullImageDTO.getRegistry();
        String imageName = pullImageDTO.getImageName();
        if (!imageName.contains("/")) {
            imageName = "library/" + imageName;
        }
        String tag = pullImageDTO.getTagName();

        Path tempDirectory = Files.createTempDirectory("docker_pull_cache_");
        log.info("tempDirectory: {}", tempDirectory);

        try {
            log.info("get manifest");
            ManifestResp tagManifests = registryApi.manifest(registry, imageName, tag, VND_DOCKER_DISTRIBUTION_MANIFEST_V2_JSON);
            log.debug("resp: {}", tagManifests);

            ManifestResp.Manifests manifests = tagManifests.getManifests().toArray(new ManifestResp.Manifests[0])[0];
            // getFsLayers
            log.info("get FsLayers");
            ManifestResp manifest = registryApi.manifest(registry, imageName, manifests.getDigest(), VND_OCI_IMAGE_MANIFEST_V1_JSON);

            // getDockerConfig
            // https://github.com/opencontainers/image-spec/blob/v1.0.1/config.md
            log.info("download docker config");
            ManifestResp.Config config = manifest.getConfig();
            InputStream configInputStream = registryApi.blobs(registry, imageName, config.getDigest());
//        InputStream configInputStream = registryApi.downloadManifest(DOCKER_REGISTRY_URL, imageName, config.getDigest(), config.getMediaType());
            // 把configBytes 写入磁盘
            Digests digests = new Digests(config.getDigest());
            String configJsonFileName = String.format("%s.json", digests.getDigest());
//        Files.write(tempDirectory.resolve(configJsonFileName), configBytes);
            log.info("write config json to {}", tempDirectory.resolve(configJsonFileName));
            Files.copy(configInputStream, tempDirectory.resolve(configJsonFileName), StandardCopyOption.REPLACE_EXISTING);
            IOUtils.close(configInputStream);

            ManifestFileDTO manifestFileDTO = new ManifestFileDTO();

            log.info("get layers");
            for (ManifestResp.Layers layer : manifest.getLayers()) {

                // 创建layer文件夹
                String digest = layer.getDigest();
                Path layerDirPath = tempDirectory.resolve(new Digests(digest).getDigest());
                File layerDir = layerDirPath.toFile();
                layerDir.mkdirs();

                // 创建VERSION 文件
                Files.write(layerDirPath.resolve("VERSION"), "1.0".getBytes());

                Path layerTarPath = layerDirPath.resolve("layer.tar");

                // layer不存在，下载
                // todo 未校验完整性
                if (!layerTarPath.toFile().exists()) {
                    // 下载layer.tar
                    InputStream layerInputSteam = registryApi.blobs(registry, imageName, digest);
//            Path layerTarGzPath = layerDirPath.resolve("layer.tar.gz");
//            Files.copy(layerInputSteam, layerTarGzPath);
//            IOUtils.close(layerInputSteam);

                    // gz 解压
//            decompressGzipFile(layerTarGzPath.toString(), layerTarPath.toString());
                    decompressGzipFile(layerInputSteam, layerTarPath.toString());
                    IOUtils.close(layerInputSteam);
                }


                String layerItem = layerTarPath.toString().replace(tempDirectory.toString() + File.separatorChar, "")
                        .replace(File.separatorChar, '/');

//            manifestFileDTO.getLayers().add(String.format("%s%s%s", File.separatorChar, new Digests(digest).getDigest(), "layer.tar"));
                manifestFileDTO.getLayers().add(layerItem);
            }

            // 最后写入
            manifestFileDTO.getRepoTags().add(String.format("%s:%s", imageName, tag));
            manifestFileDTO.setConfig(configJsonFileName);

            log.info("write manifest.json");
            Files.write(tempDirectory.resolve("manifest.json"), JsonUtil.toJson(List.of(manifestFileDTO)).getBytes());

//        Path imageFile = Files.createTempFile("docker_pull_cache_", ArchiveStreamFactory.TAR);

//        // ArchiveStreamFactory 把tempDirectory打成tar包
//        ArchiveInputStream<? extends ArchiveEntry> input = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, Files.newInputStream(tempDirectory));
//        IOUtils.copy(input, Files.newOutputStream(imageFile));
//        IOUtils.close(input);

            Path targetTarPath = Paths.get(tempDirectory.getFileName() + ".tar");

            log.info("packaging");
            createTarGzipFolder(tempDirectory, targetTarPath);

            return targetTarPath;
        } catch (Exception e) {
            log.error("pull image error", e);
            throw e;
        } finally {
            log.info("delete temp directory");
//            FileUtils.deleteDirectory(tempDirectory.toFile());
        }
    }
}
