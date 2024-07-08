//package org.example.dockerdownloader;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.HttpEntity;
//import org.apache.hc.core5.http.HttpResponse;
//import org.apache.hc.core5.http.io.entity.EntityUtils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Iterator;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
///**
// * 下面是一个简化的示例，展示了如何使用Java进行这些操作。我们将使用HTTP库（如Apache HttpClient）来与Docker Registry API进行交互，并使用文件I/O操作来处理镜像文件。
// */
//public class DockerRegistryClient1 {
//
//    private static final String DOCKER_HUB_BASE_URL = "https://registry-1.docker.io/v2/";
//
//    /**
//     * 首先，我们需要获取镜像的Manifest文件，它包含了镜像层的详细信息。
//     *
//     * @param repository
//     * @param tag
//     * @return
//     * @throws Exception
//     */
//    public static JsonNode getManifest(String repository, String tag) throws Exception {
//        String url = DOCKER_HUB_BASE_URL + repository + "/manifests/" + tag;
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        HttpGet request = new HttpGet(url);
//        request.setHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json");
//
//        HttpResponse response = httpClient.execute(request);
//        HttpEntity entity = response.getEntity();
//        String json = EntityUtils.toString(entity);
//
//        ObjectMapper mapper = new ObjectMapper();
//        return mapper.readTree(json);
//    }
//
//    /**
//     * 从Manifest中解析出镜像层的Blob并下载。
//     *
//     * @param manifest
//     * @param repository
//     * @throws Exception
//     */
//    public static void downloadLayers(JsonNode manifest, String repository) throws Exception {
//        JsonNode layers = manifest.get("layers");
//        Iterator<JsonNode> iterator = layers.elements();
//
//        while (iterator.hasNext()) {
//            JsonNode layer = iterator.next();
//            String digest = layer.get("digest").asText();
//            downloadLayer(repository, digest);
//        }
//    }
//
//    private static void downloadLayer(String repository, String digest) throws Exception {
//        String url = DOCKER_HUB_BASE_URL + repository + "/blobs/" + digest;
//        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Accept", "application/octet-stream");
//
//        try (InputStream in = connection.getInputStream();
//             FileOutputStream out = new FileOutputStream(digest + ".tar")) {
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = in.read(buffer)) != -1) {
//                out.write(buffer, 0, bytesRead);
//            }
//        }
//    }
//
//    /**
//     * 将下载的层文件打包成一个tar文件。
//     *
//     * @param layers
//     * @param outputFile
//     * @throws Exception
//     */
//    public static void exportImage(String[] layers, String outputFile) throws Exception {
//        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
//            for (String layer : layers) {
//                File file = new File(layer + ".tar");
//                try (FileInputStream fis = new FileInputStream(file)) {
//                    ZipEntry zipEntry = new ZipEntry(file.getName());
//                    zos.putNextEntry(zipEntry);
//
//                    byte[] buffer = new byte[4096];
//                    int bytesRead;
//                    while ((bytesRead = fis.read(buffer)) != -1) {
//                        zos.write(buffer, 0, bytesRead);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 这个示例代码展示了如何使用Java从Docker Hub拉取镜像并导出为tar文件。
//     * 实际应用中需要处理更多的细节和错误处理，例如身份验证、缓存、进度显示等。
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        try {
//            String repository = "library/hello-world";
//            String tag = "latest";
//            JsonNode manifest = getManifest(repository, tag);
//            downloadLayers(manifest, repository);
//
//            // 列出所有层的文件名
//            String[] layers = {"layer1", "layer2", "layer3"}; // 需要从实际下载的层文件名中获取
//            exportImage(layers, "output-image.tar");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
