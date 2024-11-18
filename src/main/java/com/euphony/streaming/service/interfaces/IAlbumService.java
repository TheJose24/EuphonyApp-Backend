package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.AlbumRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de álbumes.
 */
public interface IAlbumService {

    /**
     * Obtiene todos los álbumes registrados en el sistema.
     *
     * @return Lista de {@link AlbumResponseDTO} con la información de todos los álbumes.
     */
    List<AlbumResponseDTO> findAllAlbums();


    /**
     * Obtiene información de un álbum específico por su Nombre.
     *
     * @param name El nombre del álbum.
     * @return Un {@link AlbumResponseDTO} con los datos del álbum.
     */
    AlbumResponseDTO findAlbumByName(String name);


    /**
     * Crea un nuevo álbum en el sistema.
     *
     * @param albumRequestDTO Un objeto {@link AlbumRequestDTO} con los datos del álbum.
     * @param coverImage La imagen de portada del álbum.
     */
    void createAlbum(AlbumRequestDTO albumRequestDTO, MultipartFile coverImage) throws IOException;


    /**
     * Actualiza la información de un álbum en el sistema.
     *
     * @param id El identificador único del álbum a actualizar.
     * @param albumRequestDTO Un objeto {@link AlbumRequestDTO} con los datos actualizados del álbum.
     * @param coverImage La nueva imagen de portada del álbum.
     */
    void updateAlbum(Long id, AlbumRequestDTO albumRequestDTO, MultipartFile coverImage) throws IOException;


    /**
     * Elimina un álbum del sistema.
     *
     * @param id El identificador único del álbum a eliminar.
     */
    void deleteAlbum(Long id);
}
