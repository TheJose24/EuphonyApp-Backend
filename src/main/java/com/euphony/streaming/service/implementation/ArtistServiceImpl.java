package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.ArtistRequestDTO;
import com.euphony.streaming.dto.response.ArtistResponseDTO;
import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.exception.custom.artist.ArtistCreationException;
import com.euphony.streaming.exception.custom.artist.ArtistNotFoundException;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.service.interfaces.IArtistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class ArtistServiceImpl implements IArtistService {

    private final ArtistaRepository artistRepository;

    @Override
    public List<ArtistResponseDTO> findAllArtists() {
        return artistRepository.findAll().stream()
                .map(this::mapToArtistResponseDTO)
                .toList();
    }

    @Override
    public ArtistResponseDTO findArtistByName(String name) {
        validateName(name);
        return artistRepository.findByNombre(name)
                .map(this::mapToArtistResponseDTO)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con nombre: " + name, HttpStatus.NOT_FOUND));
    }

    @Override
    public void createArtist(ArtistRequestDTO artistRequestDTO) {
        if (Boolean.TRUE.equals(artistRepository.existsByNombre(artistRequestDTO.getName()))) {
            throw new ArtistCreationException("El artista ya existe", HttpStatus.CONFLICT);
        }

        ArtistaEntity artistaEntity = new ArtistaEntity();
        // Validar que el nombre no esté vacío o nulo
        validateName(artistRequestDTO.getName());
        artistaEntity.setNombre(artistRequestDTO.getName());
        // Validar que el país no esté vacío o nulo
        validateCountry(artistRequestDTO.getCountry());
        artistaEntity.setBiografia(artistRequestDTO.getBiography());
        artistaEntity.setPais(artistRequestDTO.getCountry());
        artistaEntity.setRedesSociales(artistRequestDTO.getSocialNetworks());

        artistRepository.save(artistaEntity);
    }

    @Override
    public void updateArtist(Long id, ArtistRequestDTO artistRequestDTO) {

        Optional<ArtistaEntity> artistaEntityOptional = artistRepository.findById(id);
        if (artistaEntityOptional.isEmpty()) {
            throw new ArtistNotFoundException("Artista no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }

        // Si hay algún campo vacío, se mantiene el valor anterior
        ArtistaEntity artistaEntity = artistaEntityOptional.get();
        artistaEntity.setNombre(artistRequestDTO.getName() != null ? artistRequestDTO.getName() : artistaEntity.getNombre());
        artistaEntity.setBiografia(artistRequestDTO.getBiography() != null ? artistRequestDTO.getBiography() : artistaEntity.getBiografia());
        artistaEntity.setPais(artistRequestDTO.getCountry() != null ? artistRequestDTO.getCountry() : artistaEntity.getPais());
        artistaEntity.setRedesSociales(artistRequestDTO.getSocialNetworks() != null ? artistRequestDTO.getSocialNetworks() : artistaEntity.getRedesSociales());

        artistRepository.save(artistaEntity);
    }

    @Override
    public void deleteArtist(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new ArtistNotFoundException("Artista no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }

        artistRepository.deleteById(id);
    }

    private ArtistResponseDTO mapToArtistResponseDTO(ArtistaEntity artistaEntity) {
        return ArtistResponseDTO.builder()
                .idArtist(artistaEntity.getIdArtista())
                .name(artistaEntity.getNombre())
                .biography(artistaEntity.getBiografia())
                .country(artistaEntity.getPais())
                .socialNetworks(artistaEntity.getRedesSociales())
                .isVerified(artistaEntity.getIsVerified())
                .build();
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ArtistCreationException("El nombre del artista no puede estar vacío", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCountry(String country) {
        if (!StringUtils.hasText(country)) {
            throw new ArtistCreationException("El país del artista no puede estar vacío", HttpStatus.BAD_REQUEST);
        }
    }

}