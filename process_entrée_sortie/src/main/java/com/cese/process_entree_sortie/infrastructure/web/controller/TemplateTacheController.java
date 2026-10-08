package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.tache.CreateTemplateTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.tache.UpdateTemplateTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates/taches")
public class TemplateTacheController {
    
    private final CreateTemplateTacheApi createTemplateTacheApi;
    private final GetTemplateTacheByIdApi getTemplateTacheByIdApi;
    private final ListTachesByGroupeTacheApi listTachesByGroupeTacheApi;
    private final UpdateTemplateTacheApi updateTemplateTacheApi;
    private final DeleteTemplateTacheApi deleteTemplateTacheApi;
    private final DuplicateTemplateTacheApi duplicateTemplateTacheApi;
    
    public TemplateTacheController(
            CreateTemplateTacheApi createTemplateTacheApi,
            GetTemplateTacheByIdApi getTemplateTacheByIdApi,
            ListTachesByGroupeTacheApi listTachesByGroupeTacheApi,
            UpdateTemplateTacheApi updateTemplateTacheApi,
            DeleteTemplateTacheApi deleteTemplateTacheApi,
            DuplicateTemplateTacheApi duplicateTemplateTacheApi) {
        this.createTemplateTacheApi = createTemplateTacheApi;
        this.getTemplateTacheByIdApi = getTemplateTacheByIdApi;
        this.listTachesByGroupeTacheApi = listTachesByGroupeTacheApi;
        this.updateTemplateTacheApi = updateTemplateTacheApi;
        this.deleteTemplateTacheApi = deleteTemplateTacheApi;
        this.duplicateTemplateTacheApi = duplicateTemplateTacheApi;
    }
    
    @PostMapping
    public ResponseEntity<TemplateTacheDTO> createTemplateTache(@RequestBody CreateTemplateTacheCommand command) {
        TemplateTacheDTO tache = createTemplateTacheApi.createTemplateTache(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(tache);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateTacheDTO> getTemplateTacheById(@PathVariable UUID id) {
        TemplateTacheDTO tache = getTemplateTacheByIdApi.getTemplateTacheById(id);
        return ResponseEntity.ok(tache);
    }
    
    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<TemplateTacheDTO>> listTachesByGroupeTache(@PathVariable UUID groupeId) {
        List<TemplateTacheDTO> taches = listTachesByGroupeTacheApi.getListTacheByGroupeTache(groupeId);
        return ResponseEntity.ok(taches);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemplateTacheDTO> updateTemplateTache(
            @PathVariable UUID id,
            @RequestBody UpdateTemplateTacheCommand command) {
        UpdateTemplateTacheCommand commandWithId = new UpdateTemplateTacheCommand(id, command.libelle(), command.description(), command.delaiJour());
        TemplateTacheDTO tache = updateTemplateTacheApi.udpateTemplateTache(commandWithId);
        return ResponseEntity.ok(tache);
    }
    
    @PostMapping("/{id}/dupliquer")
    public ResponseEntity<TemplateTacheDTO> duplicateTemplateTache(@PathVariable UUID id) {
        TemplateTacheDTO tache = duplicateTemplateTacheApi.duplicateTempalteTache(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(tache);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateTache(@PathVariable UUID id) {
        deleteTemplateTacheApi.deleteTemplateTache(id);
        return ResponseEntity.noContent().build();
    }
}
