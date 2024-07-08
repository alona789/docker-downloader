package org.example.dockerdownloader.controller;

import jakarta.validation.Valid;
import org.example.dockerdownloader.dto.PullImageDTO;
import org.example.dockerdownloader.engine.DockerClient;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * docker controller
 *
 * @author XSJ
 * @version 1.0.0
 */
@RequestMapping("/docker")
@RestController
public class DockerController {

    @GetMapping("/pull")
    public ResponseEntity<Resource> pull(@Valid PullImageDTO pullImageDTO) throws IOException {
        Path path = DockerClient.pullImage(pullImageDTO);

        PathResource resource = new PathResource(path);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", URLEncoder.encode(path.getFileName().toString(), "UTF-8")));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(resource);
    }

}
