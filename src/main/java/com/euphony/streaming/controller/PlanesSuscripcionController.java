package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.PlanesSuscripcionRequestDTO;
import com.euphony.streaming.dto.response.PlanesSuscripcionResponseDTO;
import com.euphony.streaming.service.interfaces.IPlanesSuscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Tag(name = "Gestión de Planes de Suscripción", description = "API para la gestión de planes de suscripción")
@Slf4j
public class PlanesSuscripcionController {

    private final IPlanesSuscripcionService service;

    @Operation(summary = "Obtener todos los planes de suscripción")
    @ApiResponse(responseCode = "200", description = "Lista de planes recuperada exitosamente")
    @GetMapping("/all")
    public ResponseEntity<List<PlanesSuscripcionResponseDTO>> getAllPlans() {
        log.info("Iniciando operación: Obtener todos los planes de suscripción");
        List<PlanesSuscripcionResponseDTO> plans = service.getAllPlans();
        log.info("Operación completada: Se recuperaron {} planes de suscripción", plans.size());
        return ResponseEntity.ok(plans);
    }

    @Operation(summary = "Buscar plan de suscripción por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    @GetMapping("/search/{id}")
    public ResponseEntity<PlanesSuscripcionResponseDTO> getPlanById(
            @Parameter(description = "ID del plan a buscar")
            @PathVariable Long id) {
        log.info("Iniciando operación: Buscar plan de suscripción con ID {}", id);
        PlanesSuscripcionResponseDTO plan = service.getPlanById(id);
        log.info("Operación completada: Plan de suscripción encontrado con ID {}", id);
        return ResponseEntity.ok(plan);
    }

    @Operation(summary = "Crear un nuevo plan de suscripción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan creado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El plan ya existe"),
            @ApiResponse(responseCode = "400", description = "Datos del plan inválidos")
    })
    @PostMapping("/create")
    public ResponseEntity<Void> createPlan(
            @Parameter(description = "Datos del plan a crear")
            @RequestBody PlanesSuscripcionRequestDTO requestDTO) {
        log.info("Iniciando operación: Crear un nuevo plan de suscripción");
        service.createPlan(requestDTO);
        log.info("Operación completada: Plan de suscripción creado exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Actualizar un plan de suscripción existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos del plan inválidos")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updatePlan(
            @Parameter(description = "ID del plan a actualizar")
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del plan")
            @RequestBody PlanesSuscripcionRequestDTO requestDTO) {
        log.info("Iniciando operación: Actualizar plan de suscripción con ID {}", id);
        service.updatePlan(id, requestDTO);
        log.info("Operación completada: Plan de suscripción con ID {} actualizado exitosamente", id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Eliminar un plan de suscripción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlan(
            @Parameter(description = "ID del plan a eliminar")
            @PathVariable Long id) {
        log.info("Iniciando operación: Eliminar plan de suscripción con ID {}", id);
        service.deletePlan(id);
        log.info("Operación completada: Plan de suscripción con ID {} eliminado exitosamente", id);
        return ResponseEntity.noContent().build();
    }
}
