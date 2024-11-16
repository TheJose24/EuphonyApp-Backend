package com.euphony.streaming.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz que define las operaciones de almacenamiento de archivos.
 */
public interface IFileStorageService {

    /**
     * Almacena un archivo en el sistema.
     *
     * @param file El archivo a almacenar.
     * @param contentType El tipo de contenido del archivo.
     * @return La ruta del archivo almacenado.
     */
    String storeFile(MultipartFile file, String contentType);


    /**
     * Obtiene un archivo almacenado en el sistema.
     *
     * @param filePath La ruta del archivo a obtener.
     */
    void deleteFile(String filePath);
}
