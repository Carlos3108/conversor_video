package org.azdev.conversor.controller;

import lombok.RequiredArgsConstructor;
import org.azdev.conversor.dto.VideoUploadDTO;
import org.azdev.conversor.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoUploadController {

    private final VideoService videoService;

    @PostMapping("/upload")
    public ResponseEntity<VideoUploadDTO> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String savedFileName = videoService.uploadVideo(file);

        VideoUploadDTO response = VideoUploadDTO.builder()
                .fileId(savedFileName.split("_")[0])
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .message("Video uploaded successfully")
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
