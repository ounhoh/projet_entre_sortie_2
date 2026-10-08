package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.CreateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.processus.UpdateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates/processus")
public class TemplateProcessusController {
    
    private final CreateTemplateProcessusApi createTemplateProcessusApi;
    private final ListTemplateProcessusApi listTemplateProcessusApi;
    private final GetTemplateProcessusByIdApi getTemplateProcessusByIdApi;
    private final UpdateTemplateProcessusApi updateTemplateProcessusApi;
    private final ActivateTemplateProcessusApi activateTemplateProcessusApi;
    private final DesactivateTemplateProcessusApi desactivateTemplateProcessusApi;
    private final DeleteTemplateProcessusApi deleteTemplateProcessusApi;
    private final DuplicateTemplateProcessusApi duplicateTemplateProcessusApi;
    private final GetTemplatesProcesssusByTypeApi getTemplatesProcesssusByTypeApi;
    
    public TemplateProcessusController(
            CreateTemplateProcessusApi createTemplateProcessusApi,
            ListTemplateProcessusApi listTemplateProcessusApi,
            GetTemplateProcessusByIdApi getTemplateProcessusByIdApi,
            UpdateTemplateProcessusApi updateTemplateProcessusApi,
            ActivateTemplateProcessusApi activateTemplateProcessusApi,
            DesactivateTemplateProcessusApi desactivateTemplateProcessusApi,
            DeleteTemplateProcessusApi deleteTemplateProcessusApi,
            DuplicateTemplateProcessusApi duplicateTemplateProcessusApi,
            GetTemplatesProcesssusByTypeApi getTemplatesProcesssusByTypeApi) {
        this.createTemplateProcessusApi = createTemplateProcessusApi;
        this.listTemplateProcessusApi = listTemplateProcessusApi;
        this.getTemplateProcessusByIdApi = getTemplateProcessusByIdApi;
        this.updateTemplateProcessusApi = updateTemplateProcessusApi;
        this.activateTemplateProcessusApi = activateTemplateProcessusApi;
        this.desactivateTemplateProcessusApi = desactivateTemplateProcessusApi;
        this.deleteTemplateProcessusApi = deleteTemplateProcessusApi;
        this.duplicateTemplateProcessusApi = duplicateTemplateProcessusApi;
        this.getTemplatesProcesssusByTypeApi = getTemplatesProcesssusByTypeApi;
    }
    
    @PostMapping
    public ResponseEntity<TemplateProcessusDTO> createTemplateProcessus(@RequestBody CreateTemplateProcessusCommand command) {
        TemplateProcessusDTO template = createTemplateProcessusApi.createTemplateProcessus(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }
    
    @GetMapping
    public ResponseEntity<List<TemplateProcessusDTO>> listTemplateProcessus(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        com.cese.process_entree_sortie.application.dto.template.entree.processus.ListTemplateQuery query = 
            new com.cese.process_entree_sortie.application.dto.template.entree.processus.ListTemplateQuery(false, type);
        List<TemplateProcessusDTO> templates = listTemplateProcessusApi.getListTemplateProcessus(query);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateProcessusDTO> getTemplateProcessusById(@PathVariable UUID id) {
        TemplateProcessusDTO template = getTemplateProcessusByIdApi.getTemplateProcessusById(id);
        return ResponseEntity.ok(template);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TemplateProcessusDTO>> getTemplatesByType(@PathVariable String type) {
        List<TemplateProcessusDTO> templates = getTemplatesProcesssusByTypeApi.getTemplateProcessusByType(type);
        return ResponseEntity.ok(templates);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemplateProcessusDTO> updateTemplateProcessus(
            @PathVariable UUID id,
            @RequestBody UpdateTemplateProcessusCommand command) {
        UpdateTemplateProcessusCommand commandWithId = new UpdateTemplateProcessusCommand(
            id, command.libelle(), command.description(), command.statutList(), command.groupeTaches());
        TemplateProcessusDTO template = updateTemplateProcessusApi.updateTemplateProcessus(commandWithId);
        return ResponseEntity.ok(template);
    }
    
    @PutMapping("/{id}/activer")
    public ResponseEntity<TemplateProcessusDTO> activateTemplateProcessus(@PathVariable UUID id) {
        TemplateProcessusDTO template = activateTemplateProcessusApi.activateTemplateProcessus(id);
        return ResponseEntity.ok(template);
    }
    
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus> desactivateTemplateProcessus(@PathVariable UUID id) {
        com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus template = desactivateTemplateProcessusApi.desactivateProcessus(id);
        return ResponseEntity.ok(template);
    }
    
    @PostMapping("/{id}/dupliquer")
    public ResponseEntity<TemplateProcessusDTO> duplicateTemplateProcessus(@PathVariable UUID id) {
        TemplateProcessusDTO template = duplicateTemplateProcessusApi.duplicateTemplateProcesus(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateProcessus(@PathVariable UUID id) {
        deleteTemplateProcessusApi.deleteTemplateProcessu(id);
        return ResponseEntity.noContent().build();
    }
}
