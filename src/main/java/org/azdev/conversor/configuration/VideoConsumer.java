package org.azdev.conversor.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.azdev.conversor.service.VideoConversionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VideoConsumer {
    private final VideoConversionService conversionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processVideo(String originalFileName) {
        log.info("Received message to process video: {}", originalFileName);
        try {
            conversionService.processAndConvertVideo(originalFileName);
            log.info("Video processed successfully: {}", originalFileName);
        } catch (Exception e) {
            log.error("Error processing video: {}. Erro: {}", originalFileName, e.getMessage());
        }
    }
}
