package com.whurs.config;

import com.whurs.properties.AliOssProperties;
import com.whurs.properties.MinioProperties;
import com.whurs.utils.AliOssUtil;
import com.whurs.utils.MinioUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MinioProperties minioProperties){
        log.info("开始创建minio对象:{}",minioProperties);
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint()+":"+minioProperties.getPort())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
    @Bean
    public MinioUtil minioUtil(MinioProperties minioProperties, MinioClient minioClient){
        return new MinioUtil(
                minioProperties.getEndpoint(),
                minioProperties.getPort(),
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey(),
                minioProperties.getBucketName(),
                minioClient
        );
    }

}
