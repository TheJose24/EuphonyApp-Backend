package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.GenreRequestDTO;
import com.euphony.streaming.dto.response.GenreResponseDTO;
import com.euphony.streaming.entity.GeneroEntity;
import com.euphony.streaming.exception.custom.genre.GenreCreationException;
import com.euphony.streaming.exception.custom.genre.GenreNotFoundException;
import com.euphony.streaming.repository.GeneroRepository;
import com.euphony.streaming.service.interfaces.IGenreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class GenreServiceImpl implements IGenreService {

    private final GeneroRepository generoRepository;


    @Override
    public List<GenreResponseDTO> findAllGenres() {
        return generoRepository.findAll().stream()
                .map(this::mapToGenreResponseDTO)
                .toList();
    }

    @Override
    public GenreResponseDTO findGenreByName(String name) {
        return generoRepository.findByNombre(name)
                .map(this::mapToGenreResponseDTO)
                .orElseThrow(() -> new GenreNotFoundException("Género no encontrado con nombre: " + name, HttpStatus.NOT_FOUND));
    }

    @Override
    public void createGenre(GenreRequestDTO genreRequestDTO) {
        if (Boolean.TRUE.equals(generoRepository.existsByNombre(genreRequestDTO.getName()))) {
            throw new GenreCreationException("El género ya existe", HttpStatus.CONFLICT);
        }

        GeneroEntity generoEntity = new GeneroEntity();

        // Validar que el nombre no esté vacío
        if (genreRequestDTO.getName() == null || genreRequestDTO.getName().isBlank()) {
            throw new GenreCreationException("El nombre del género no puede estar vacío", HttpStatus.BAD_REQUEST);
        }

        generoEntity.setNombre(genreRequestDTO.getName());
        generoEntity.setDescripcion(genreRequestDTO.getDescription());

        generoRepository.save(generoEntity);
    }

    @Override
    public void updateGenre(Long id, GenreRequestDTO genreRequestDTO) {
        Optional<GeneroEntity> generoEntityOptional = generoRepository.findById(id);
        if (generoEntityOptional.isEmpty()) {
            throw new GenreNotFoundException("Género no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }

        // Si hay algún campo vacío, se mantiene el valor anterior
        GeneroEntity generoEntity = generoEntityOptional.get();
        generoEntity.setNombre(genreRequestDTO.getName() != null ? genreRequestDTO.getName() : generoEntity.getNombre());
        generoEntity.setDescripcion(genreRequestDTO.getDescription() != null ? genreRequestDTO.getDescription() : generoEntity.getDescripcion());

        generoRepository.save(generoEntity);
    }

    @Override
    public void deleteGenre(Long id) {
        if (!generoRepository.existsById(id)) {
            throw new GenreNotFoundException("Género no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }

        generoRepository.deleteById(id);
    }

    private GenreResponseDTO mapToGenreResponseDTO(GeneroEntity generoEntity) {
        return GenreResponseDTO.builder()
                .idGenre(generoEntity.getIdGenero())
                .name(generoEntity.getNombre())
                .description(generoEntity.getDescripcion())
                .build();
    }
}