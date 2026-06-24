package com.euphony.streaming.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@Hidden
@Slf4j
public class ImagesController {

    private static final Map<String, Path> VALID_PATHS = Map.of(
            "images", Paths.get("uploads/images"),
            "profiles", Paths.get("uploads/profiles")
    );

    @GetMapping("/uploads/{type}/{filename:.+}")
    public ResponseEntity<Resource> serveUploadedFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            if (!VALID_PATHS.containsKey(type)) {
                log.warn("Tipo de archivo inválido: {}", type);
                return ResponseEntity.badRequest().body(null);
            }

            log.info("Archivo solicitado: {}", filename);

            String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

            log.info("Archivo sanitizado: {}", sanitizedFilename);

            Path basePath = VALID_PATHS.get(type).toAbsolutePath().normalize();
            Path file = basePath.resolve(sanitizedFilename).normalize();

            if (!file.startsWith(basePath)) {
                log.warn("Intento de path traversal detectado: {}", filename);
                return ResponseEntity.badRequest().body(null);
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Archivo no encontrado o no legible: {}", filename);
                return ResponseEntity.notFound().build();
            }

            log.info("Archivo encontrado y accesible: {}", file);

            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizedFilename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error al servir el archivo: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
