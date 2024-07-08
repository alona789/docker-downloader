package org.example.dockerdownloader.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.example.dockerdownloader.constans.DockerConstants;

/**
 * @author XSJ
 * @version 1.0.0
 */
@Data
public class PullImageDTO {

    private String registry = DockerConstants.DOCKER_REGISTRY_URL;

    @NotEmpty(message = "imageName不能为空")
    private String imageName;

    private String tagName = "latest";
}
