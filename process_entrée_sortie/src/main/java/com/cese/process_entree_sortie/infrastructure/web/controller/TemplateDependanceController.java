package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.dependance.CreateTemplateDependanceCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.application.port.in.template.dependance.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates/dependances")
public class TemplateDependanceController {
    
    private final CreateTemplateDependanceApi createTemplateDependanceApi;
    private final DeleteTemplateDependanceApi deleteTemplateDependanceApi;
    private final GetDependanceByTemplateProcessusApi getDependanceByTemplateProcessusApi;
    private final ValidateTemplateDependancesApi validateTemplateDependancesApi;
    
    public TemplateDependanceController(
            CreateTemplateDependanceApi createTemplateDependanceApi,
            DeleteTemplateDependanceApi deleteTemplateDependanceApi,
            GetDependanceByTemplateProcessusApi getDependanceByTemplateProcessusApi,
            ValidateTemplateDependancesApi validateTemplateDependancesApi) {
        this.createTemplateDependanceApi = createTemplateDependanceApi;
        this.deleteTemplateDependanceApi = deleteTemplateDependanceApi;
        this.getDependanceByTemplateProcessusApi = getDependanceByTemplateProcessusApi;
        this.validateTemplateDependancesApi = validateTemplateDependancesApi;
    }
    
    @PostMapping
    public ResponseEntity<TemplateDependanceDTO> createTemplateDependance(@RequestBody CreateTemplateDependanceCommand command) {
        TemplateDependanceDTO dependance = createTemplateDependanceApi.createTemplateDependance(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(dependance);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateDependance(@PathVariable UUID id) {
        deleteTemplateDependanceApi.deleteTemplateDependance(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/template-processus/{templateProcessusId}")
    public ResponseEntity<List<TemplateDependanceDTO>> getDependanceByTemplateProcessus(@PathVariable UUID templateProcessusId) {
        List<TemplateDependanceDTO> dependances = getDependanceByTemplateProcessusApi.getDependanceByTemplateProcessus(templateProcessusId);
        return ResponseEntity.ok(dependances);
    }
    
    @PostMapping("/validate/{templateProcessusId}")
    public ResponseEntity<Boolean> validateTemplateDependances(@PathVariable UUID templateProcessusId) {
        Boolean isValid = validateTemplateDependancesApi.ValidateTemplateDependances(templateProcessusId);
        return ResponseEntity.ok(isValid);
    }
}
