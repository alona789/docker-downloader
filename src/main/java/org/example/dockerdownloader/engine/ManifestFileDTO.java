package org.example.dockerdownloader.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 清单文件的DTO
 *
 * 注意，实际写入的 manifest.json 文件的数据，不是这个类，而是这个类的集合
 *
 * @author XSJ
 * @version 1.0.0
 */
@Data
public class ManifestFileDTO {

    @JsonProperty("Config")
    private String config;

    @JsonProperty("RepoTags")
    private Collection<String> repoTags = new ArrayList<>();

    @JsonProperty("Layers")
    private Collection<String> layers = new ArrayList<>();
}
