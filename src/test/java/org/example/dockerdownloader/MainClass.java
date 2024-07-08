package org.example.dockerdownloader;

/**
 * @author XSJ
 * @version 1.0.0
 */
public class MainClass {
    public static void main(String[] args) {
        String url = "Bearer realm=\"https://auth.docker.io/token\",service=\"registry.docker.io\",scope=\"repository:library/hello-world:pull\"";
        String template = "Bearer realm=\"%s\",service=\"%s\",scope=\"%s\"";
        String[] split = url.split(",");
        for (String s : split) {
            String[] split1 = s.split("=");
            System.out.println(split1[1].replace("\"", ""));
        }
    }
}
