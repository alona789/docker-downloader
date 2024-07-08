package org.example.dockerdownloader.engine;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.HTTPProxy;
import com.dtflys.forest.annotation.Var;
import org.example.dockerdownloader.engine.response.ManifestResp;

import java.io.InputStream;

/**
 * API
 *
 * @author XSJ
 * @version 1.0.0
 * @see <a href="https://distribution.github.io/distribution/spec/api/">Docker Registry API</a>
 */
@BaseRequest(interceptor = DockerClinetInterceptor.class)
@HTTPProxy(host = "127.0.0.1", port = "10809")
public interface DockerRegistryApi {
    /**
     * 拉取清单
     *
     * @param url       仓库的地址
     * @param name      储存库的命名空间
     * @param reference 清单的摘要或标签名称
     * @return
     */
    @Get(url = "{url}/v2/{name}/manifests/{reference}", headers = {
            "Accept: ${accept}",
//            "Accept: application/vnd.oci.image.manifest.v1+json"
    })
    ManifestResp manifest(@Var("url") String url,
                          @Var("name") String name, @Var("reference") String reference,
                          @Var("accept") String accept);

    /**
     * 下载清单
     *
     * @param url       仓库的地址
     * @param name      储存库的命名空间
     * @param reference 清单的摘要或标签名称
     * @return
     */
    @Get(url = "{url}/v2/{name}/manifests/{reference}", headers = {
            "Accept: ${accept}",
    })
    InputStream downloadManifest(@Var("url") String url,
                                 @Var("name") String name, @Var("reference") String reference,
                                 @Var("accept") String accept);

    @Get(url = "{url}/v2/{name}/blobs/{digest}", headers = {
            "Accept: application/octet-stream",
    })
    InputStream blobs(@Var("url") String url,
                      @Var("name") String name, @Var("digest") String digest);

//    @Get(url = "{url}/v2/{repository}/blobs/{digest}")
//    LayerResp layer(@Var("url") String url, @Var("repository") String repository, @Var("digest") String digest);

}
