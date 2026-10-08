package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.tache.entree.CreateDependanceCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.application.port.in.dependance.CreateDependanceApi;
import com.cese.process_entree_sortie.application.port.in.dependance.DeleteDependanceApi;
import com.cese.process_entree_sortie.application.port.in.dependance.GetDependanceByProcessusApi;
import com.cese.process_entree_sortie.application.port.in.dependance.GetDependanceTacheApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dependances")
public class DependanceController {
    
    private final CreateDependanceApi createDependanceApi;
    private final DeleteDependanceApi deleteDependanceApi;
    private final GetDependanceByProcessusApi getDependanceByProcessusApi;
    private final GetDependanceTacheApi getDependanceTacheApi;
    
    public DependanceController(
            CreateDependanceApi createDependanceApi,
            DeleteDependanceApi deleteDependanceApi,
            GetDependanceByProcessusApi getDependanceByProcessusApi,
            GetDependanceTacheApi getDependanceTacheApi) {
        this.createDependanceApi = createDependanceApi;
        this.deleteDependanceApi = deleteDependanceApi;
        this.getDependanceByProcessusApi = getDependanceByProcessusApi;
        this.getDependanceTacheApi = getDependanceTacheApi;
    }
    
    @PostMapping
    public ResponseEntity<com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO> createDependance(@RequestBody CreateDependanceCommand command) {
        com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO dependance = createDependanceApi.createDependance(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(dependance);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDependance(@PathVariable UUID id) {
        deleteDependanceApi.deleteDependance(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/processus/{processusId}")
    public ResponseEntity<List<DependanceDTO>> getDependanceByProcessus(@PathVariable UUID processusId) {
        List<DependanceDTO> dependances = getDependanceByProcessusApi.getDependanceByProcessus(processusId);
        return ResponseEntity.ok(dependances);
    }
    
    @GetMapping("/tache/{tacheId}")
    public ResponseEntity<DependanceDTO> getDependanceTache(@PathVariable UUID tacheId) {
        DependanceDTO dependance = getDependanceTacheApi.getDependanceTache(tacheId);
        return ResponseEntity.ok(dependance);
    }
}
