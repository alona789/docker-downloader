package org.example.dockerdownloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractTemplateVariables {
    public static void main(String[] args) {
        String url = "Bearer realm=\"https://auth.docker.io/token\",service=\"registry.docker.io\",scope=\"repository:library/hello-world:pull\"";
        String template = "Bearer realm=\"${url}\",service=\"${service}\",scope=\"${scope}\"";

        // 定义正则表达式来匹配变量
        Pattern pattern = Pattern.compile("Bearer realm=\"(.*?)\",service=\"(.*?)\",scope=\"(.*?)\"");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            // 提取变量
            String extractedUrl = matcher.group(1);
            String extractedService = matcher.group(2);
            String extractedScope = matcher.group(3);

            System.out.println("Extracted url: " + extractedUrl);
            System.out.println("Extracted service: " + extractedService);
            System.out.println("Extracted scope: " + extractedScope);
        } else {
            System.out.println("No match found.");
        }
    }
}
