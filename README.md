# docker-downloader

## 介绍

docker-downloader是一个用于下载docker镜像的工具。
这里通过dockerhub的api拉去docker镜像的layer和config，本地打包成tar

## 快速开始

输入镜像的完整名称，例如：hello-world，输入tagName，比如latest，然后点下载，等待下载完成。

拿到docker镜像的tar文件，就可以直接使用docker load命令导入到本地。

```shell
# Load images from STDIN
docker load < busybox.tar.gz

# Load images from a file (--input)
docker load --input busybox.tar.gz
```

## TODO

- [ ] proxy 前端配置
- [ ] 支持解析自定义源

## 参考文档

1. dockerHub认证：https://distribution.github.io/distribution/spec/auth/jwt/
2. 令牌范围和访问：https://distribution.github.io/distribution/spec/auth/scope/
3. 镜像清单：https://distribution.github.io/distribution/spec/manifest-v2-2/
4. 镜像清单：https://github.com/opencontainers/image-spec/blob/main/manifest.md
5. HTTP API：https://distribution.github.io/distribution/spec/api/
6. https://spring.io/projects/spring-shell
7. https://github.com/opencontainers/distribution-spec/blob/v1.0.1/spec.md#endpoints
8. https://github.com/NotGlop/docker-drag/blob/master/docker_pull.py
9. docker load命令：https://docs.docker.com/reference/cli/docker/image/load/#input