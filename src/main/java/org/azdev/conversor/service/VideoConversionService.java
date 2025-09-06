package org.azdev.conversor.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoConversionService {

    private final MinioClient minioClient;

    @Value("${minio.buckets.originals}")
    private String originalBucket;

    @Value("${minio.buckets.processed}")
    private String processedBucket;

    private static final Map<String, String> RESOLUTIONS = Map.of(
            "1080p", "1920x1080",
            "720p", "1280x720",
            "480p", "854x480"
    );

    public void processAndConvertVideo(String originalFileName) throws Exception {
        Path tempDir = Files.createTempDirectory("video-processing-");
        Path originalFilePath = tempDir.resolve(originalFileName);

        try {
            log.info("Baixando o arquivo {} do bucket {}", originalFileName, originalBucket);
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(originalBucket).object(originalFileName).build())) {
                Files.copy(stream, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            for (Map.Entry<String, String> entry : RESOLUTIONS.entrySet()) {
                String resolutionKey = entry.getKey();

                String outputFileName = resolutionKey + "-" + originalFileName;
                Path outputFilePath = tempDir.resolve(outputFileName);

                log.info("Convertendo o vídeo {} para a resolução {}", originalFileName, resolutionKey);

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", originalFilePath.toString(),
                        "-vf", "scale=" + entry.getValue(),
                        "-preset", "slow",
                        "-crf", "22",
                        outputFilePath.toString()
                );

                Process process = processBuilder.inheritIO().start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new RuntimeException("Erro na conversão para " + resolutionKey);
                }

                log.info("Fazendo upload de {} para o bucket {}", outputFileName, processedBucket);
                try (InputStream convertedStream = Files.newInputStream(outputFilePath)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(processedBucket)
                                    .object(outputFileName)
                                    .stream(convertedStream, outputFilePath.toFile().length(), -1)
                                    .build()
                    );
                }
            }
        } finally {
            log.info("Limpando arquivos temporários em {}", tempDir);
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.deleteIfExists(tempDir);
        }
    }
}
