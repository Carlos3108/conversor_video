package org.azdev.conversor.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.azdev.conversor.configuration.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
    private final MinioClient minioClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${minio.buckets.originals}")
    private String originalBucketName;

    public String uploadVideo(MultipartFile file) {
        try {
            String fileId = UUID.randomUUID().toString();
            String fileName = fileId + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(originalBucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Video uploaded successfully: {}", fileName);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY, fileName);
            log.info("Mensagem para processamento do vídeo {} enviada para a exchange.", fileName);

            return fileName;
        } catch (Exception e) {
            log.error("Error uploading video", e);
            throw new RuntimeException("Falha no upload do vídeo", e);
        }
    }
}
