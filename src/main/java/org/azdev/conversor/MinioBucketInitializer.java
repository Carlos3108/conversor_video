package org.azdev.conversor;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioBucketInitializer implements ApplicationRunner {
    private final MinioClient minioClient;

    @Value("${minio.buckets.originals}")
    private String originalBucketName;

    @Value("${minio.buckets.processed}")
    private String processedBucketName;


    public void run(ApplicationArguments args) throws Exception {
        log.info("Iniciando verificação dos buckets no MinIO...");

        List<String> bucketsNames = List.of(originalBucketName, processedBucketName);

        for (String bucketName : bucketsNames) {
            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

                if(found) {
                    log.info("Bucket '{}' já existe.", bucketName);
                } else {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.warn("Bucket '{}' criado com sucesso.", bucketName);
                }
            } catch (Exception e) {
                log.error("Erro ao verificar/criar o bucket '{}': {}", bucketName, e.getMessage());
                throw new RuntimeException("Falha ao inicializar os buckets no MinIO", e);
            }
        log.info("Verificação dos buckets no MinIO concluída.");
        }
    }
}
