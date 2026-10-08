package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.direction.entree.CreateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.entree.UpdateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;
import com.cese.process_entree_sortie.application.port.in.direction.createDirectionApi;
import com.cese.process_entree_sortie.application.port.in.direction.DeleteDirectionApi;
import com.cese.process_entree_sortie.application.port.in.direction.GetDirectionByIdApi;
import com.cese.process_entree_sortie.application.port.in.direction.ListDirectionApi;
import com.cese.process_entree_sortie.application.port.in.direction.UpdateDirectionApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/directions")
public class DirectionController {
    
    private final createDirectionApi createDirectionApi;
    private final ListDirectionApi listDirectionApi;
    private final GetDirectionByIdApi getDirectionByIdApi;
    private final UpdateDirectionApi updateDirectionApi;
    private final DeleteDirectionApi deleteDirectionApi;
    
    public DirectionController(
            createDirectionApi createDirectionApi,
            ListDirectionApi listDirectionApi,
            GetDirectionByIdApi getDirectionByIdApi,
            UpdateDirectionApi updateDirectionApi,
            DeleteDirectionApi deleteDirectionApi) {
        this.createDirectionApi = createDirectionApi;
        this.listDirectionApi = listDirectionApi;
        this.getDirectionByIdApi = getDirectionByIdApi;
        this.updateDirectionApi = updateDirectionApi;
        this.deleteDirectionApi = deleteDirectionApi;
    }
    
    @PostMapping
    public ResponseEntity<DirectionDTO> createDirection(@RequestBody CreateDirectionCommand command) {
        DirectionDTO direction = createDirectionApi.createDirection(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(direction);
    }
    
    @GetMapping
    public ResponseEntity<List<DirectionDTO>> listDirections() {
        List<DirectionDTO> directions = listDirectionApi.getListDirection();
        return ResponseEntity.ok(directions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DirectionDTO> getDirectionById(@PathVariable UUID id) {
        DirectionDTO direction = getDirectionByIdApi.getDirectionById(id);
        return ResponseEntity.ok(direction);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DirectionDTO> updateDirection(
            @PathVariable UUID id,
            @RequestBody UpdateDirectionCommand command) {
        UpdateDirectionCommand commandWithId = new UpdateDirectionCommand(id, command.code(), command.libelle());
        DirectionDTO direction = updateDirectionApi.updateDirection(commandWithId);
        return ResponseEntity.ok(direction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirection(@PathVariable UUID id) {
        deleteDirectionApi.deleteDirection(id);
        return ResponseEntity.noContent().build();
    }
}
