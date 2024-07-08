package org.example.dockerdownloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class DockerRegistryClient {
    private static final String DOCKER_HUB_BASE_URL = "https://registry-1.docker.io/v2/";

    private static final CloseableHttpClient httpClient;

    private static String token;
    private static String authUrl;
    private static String regService;

    static {
        httpClient = HttpClientBuilder.create()
//                .setRetryStrategy(new DefaultHttpRequestRetryStrategy() {
//
//                    @Override
//                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
//                        log.info("retry request {}", exception.getMessage());
//                        return super.retryRequest(request, exception, execCount, context);
//                    }
//
//                    @Override
//                    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
//                        int code = response.getCode();
//                        if (code == 401) {
//                            log.info("retry request {}", response.getCode());
//
//                            String[] authenticateArr = response.getHeader("WWW-Authenticate").getValue().split("\"");
//                            authUrl = authenticateArr[1];
//                            regService = authenticateArr[3];
//                            String repository = authenticateArr[5];
//                            try {
//                                String token = authorize(repository, "");
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                            return false;
//                        }
//                        return super.retryRequest(response, execCount, context);
//                    }
//                })
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * 授权方法
     */
    public static String authorize(String repository) throws IOException, ParseException {
        HttpGet request = new HttpGet(String.format("%s?service=%s&scope=repository:%s:pull", authUrl, regService, repository));
//        request.setHeader("Authorization", "");
//        request.setHeader("Accept", type);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new HttpResponseException(response.getCode(), "Failed to fetch token");
            }
            String json = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            String token = rootNode.get("token").asText();
            Header[] headers = response.getHeaders();
            for (Header header : headers) {
                System.out.println(header.getName() + ":" + header.getValue());
            }
            return token;
        }
    }

    public static JsonNode getManifest(String repository, String tag) throws IOException, ParseException {
        String url = DOCKER_HUB_BASE_URL + repository + "/manifests/" + tag;
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        request.setHeader("Authorization", String.format("Bearer %s", token));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 401) {
                String[] authenticateArr = response.getHeader("WWW-Authenticate").getValue().split("\"");
                authUrl = authenticateArr[1];
                regService = authenticateArr[3];
                token = authorize(repository);
                return getManifest(repository, tag);
            }
            if (response.getCode() != 200) {
                throw new HttpResponseException(response.getCode(), "Failed to fetch manifest");
            }
            String json = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(json);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }


    public static void downloadLayers(JsonNode manifest, String repository) throws IOException {
        JsonNode layers = manifest.get("layers");
        Iterator<JsonNode> iterator = layers.elements();

        while (iterator.hasNext()) {
            JsonNode layer = iterator.next();
            String digest = layer.get("digest").asText();
            downloadLayer(repository, digest);
        }
    }

    private static void downloadLayer(String repository, String digest) throws IOException {
        String url = DOCKER_HUB_BASE_URL + repository + "/blobs/" + digest;
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/octet-stream");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new HttpResponseException(response.getCode(), "Failed to fetch layer: " + digest);
            }
            try (InputStream in = response.getEntity().getContent();
                 FileOutputStream out = new FileOutputStream(digest.substring(7) + ".tar")) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static void exportImage(String[] layers, String outputFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            for (String layer : layers) {
                File file = new File(layer + ".tar");
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            String repository = "library/hello-world";
            String tag = "latest";
            JsonNode manifest = getManifest(repository, tag);
            downloadLayers(manifest, repository);

            // 列出所有层的文件名
            String[] layers = manifest.get("layers")
                    .findValuesAsText("digest")
                    .stream()
                    .map(digest -> digest.substring(7)) // 去除前缀 "sha256:"
                    .toArray(String[]::new);
            exportImage(layers, "output-image.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
