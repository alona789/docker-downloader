package org.example.dockerdownloader.engine;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import org.example.dockerdownloader.engine.response.AuthResp;

public interface DockerAuthApi {

    @Get(url = "{url}?service={service}&scope=repository:{repository}:pull")
    AuthResp auth(@Var("url") String url,
                  @Var("service") String service, @Var("repository") String repository);

}
