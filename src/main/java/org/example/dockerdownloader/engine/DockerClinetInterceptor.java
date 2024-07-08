package org.example.dockerdownloader.engine;

import com.dtflys.forest.Forest;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.http.HttpStatus;
import com.dtflys.forest.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DockerClinetInterceptor<T> implements Interceptor<T> {
    private final static Logger log = LoggerFactory.getLogger(DockerClinetInterceptor.class);

    private static Map<String, String> cache = new ConcurrentHashMap<>(8);

    static {
        cache.put("authUrl", "https://auth.docker.io/token");
        cache.put("manifestUrl", "https://registry-1.docker.io/v2/{repository}/manifests/{tag}");
    }


    private void addHeader(ForestRequest req) {
        String repository = ((String) req.variableValue("name"));
        String token = cache.get("token");
        if (token == null) {
            synchronized (this) {
                token = cache.get("token");
                if (token == null) {
                    DockerAuthApi dockerAuthApi = Forest.client(DockerAuthApi.class);
                    token = dockerAuthApi.auth(cache.get("authUrl"), "registry.docker.io", repository).getToken();
                    cache.put("token", token);
                }
            }
        }
        req.addHeader("Authorization", "Bearer " + token);
    }


    /**
     * 该方法在请求发送之前被调用, 若返回false则不会继续发送请求
     *
     * @Param request Forest请求对象
     */
    @Override
    public boolean beforeExecute(ForestRequest req) {
        log.info("invoke Simple beforeExecute");
        addHeader(req);
        return true;
    }

    /**
     * 该方法在请求发送失败时被调用
     */
    @Override
    public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
        log.info("invoke Simple onError");
        if (res.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            cache.remove("token");
            addHeader(req);
        }
    }
}

