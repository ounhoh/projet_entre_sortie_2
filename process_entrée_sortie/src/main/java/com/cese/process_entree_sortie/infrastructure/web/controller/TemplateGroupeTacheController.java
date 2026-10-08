package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.AddTacheToGroupTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.CreateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.RemoveTacheFromTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.ReorderTacheInGroupeCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.UpdateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates/groupes-taches")
public class TemplateGroupeTacheController {
    
    private final CreateTemplateGroupeTacheApi createTemplateGroupeTacheApi;
    private final GetTemplateGroupeTacheByIdApi getTemplateGroupeTacheByIdApi;
    private final UpdateTemplateGroupeTacheApi updateTemplateGroupeTacheApi;
    private final DeleteTemplateGroupeTacheApi deleteTemplateGroupeTacheApi;
    private final DuplicateTemplateGroupeTacheApi duplicateTemplateGroupeTacheApi;
    private final ListGroupsTacheByTemplateProcessusApi listGroupsTacheByTemplateProcessusApi;
    private final AddTacheToGroupTacheApi addTacheToGroupTacheApi;
    private final RemoveTacheFromTemplateGroupeTacheApi removeTacheFromTemplateGroupeTacheApi;
    private final ReorderTachesInTemplateGroupeApi reorderTachesInTemplateGroupeApi;
    
    public TemplateGroupeTacheController(
            CreateTemplateGroupeTacheApi createTemplateGroupeTacheApi,
            GetTemplateGroupeTacheByIdApi getTemplateGroupeTacheByIdApi,
            UpdateTemplateGroupeTacheApi updateTemplateGroupeTacheApi,
            DeleteTemplateGroupeTacheApi deleteTemplateGroupeTacheApi,
            DuplicateTemplateGroupeTacheApi duplicateTemplateGroupeTacheApi,
            ListGroupsTacheByTemplateProcessusApi listGroupsTacheByTemplateProcessusApi,
            AddTacheToGroupTacheApi addTacheToGroupTacheApi,
            RemoveTacheFromTemplateGroupeTacheApi removeTacheFromTemplateGroupeTacheApi,
            ReorderTachesInTemplateGroupeApi reorderTachesInTemplateGroupeApi) {
        this.createTemplateGroupeTacheApi = createTemplateGroupeTacheApi;
        this.getTemplateGroupeTacheByIdApi = getTemplateGroupeTacheByIdApi;
        this.updateTemplateGroupeTacheApi = updateTemplateGroupeTacheApi;
        this.deleteTemplateGroupeTacheApi = deleteTemplateGroupeTacheApi;
        this.duplicateTemplateGroupeTacheApi = duplicateTemplateGroupeTacheApi;
        this.listGroupsTacheByTemplateProcessusApi = listGroupsTacheByTemplateProcessusApi;
        this.addTacheToGroupTacheApi = addTacheToGroupTacheApi;
        this.removeTacheFromTemplateGroupeTacheApi = removeTacheFromTemplateGroupeTacheApi;
        this.reorderTachesInTemplateGroupeApi = reorderTachesInTemplateGroupeApi;
    }
    
    @PostMapping
    public ResponseEntity<TemplateGroupeTacheDTO> createTemplateGroupeTache(@RequestBody CreateTemplateGroupeTacheCommand command) {
        TemplateGroupeTacheDTO groupe = createTemplateGroupeTacheApi.createTemplateGroupeTache(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupe);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateGroupeTacheDTO> getTemplateGroupeTacheById(@PathVariable UUID id) {
        TemplateGroupeTacheDTO groupe = getTemplateGroupeTacheByIdApi.getTemplateGroupeTacheById(id);
        return ResponseEntity.ok(groupe);
    }
    
    @GetMapping("/template-processus/{templateProcessusId}")
    public ResponseEntity<List<TemplateGroupeTacheDTO>> listGroupsByTemplateProcessus(@PathVariable UUID templateProcessusId) {
        List<TemplateGroupeTacheDTO> groupes = listGroupsTacheByTemplateProcessusApi.getListGroupTacheByTemplateProcessus(templateProcessusId);
        return ResponseEntity.ok(groupes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemplateGroupeTacheDTO> updateTemplateGroupeTache(
            @PathVariable UUID id,
            @RequestBody UpdateTemplateGroupeTacheCommand command) {
        UpdateTemplateGroupeTacheCommand commandWithId = new UpdateTemplateGroupeTacheCommand(id, command.libelle(), command.ordre(), command.directionId());
        TemplateGroupeTacheDTO groupe = updateTemplateGroupeTacheApi.updateGroupeTache(commandWithId);
        return ResponseEntity.ok(groupe);
    }
    
    @PostMapping("/{id}/dupliquer")
    public ResponseEntity<TemplateGroupeTacheDTO> duplicateTemplateGroupeTache(@PathVariable UUID id) {
        TemplateGroupeTacheDTO groupe = duplicateTemplateGroupeTacheApi.duplicateTemplateGroupeTache(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupe);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateGroupeTache(@PathVariable UUID id) {
        deleteTemplateGroupeTacheApi.deleteGroupeTache(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/taches")
    public ResponseEntity<TemplateGroupeTacheDTO> addTacheToGroupTache(
            @PathVariable UUID id,
            @RequestBody AddTacheToGroupTacheCommand command) {
        AddTacheToGroupTacheCommand commandWithId = new AddTacheToGroupTacheCommand(id, command.tacheId());
        TemplateGroupeTacheDTO groupe = addTacheToGroupTacheApi.addTacheToGroupeTache(commandWithId);
        return ResponseEntity.ok(groupe);
    }
    
    @DeleteMapping("/{id}/taches/{tacheId}")
    public ResponseEntity<TemplateGroupeTacheDTO> removeTacheFromTemplateGroupeTache(
            @PathVariable UUID id,
            @PathVariable UUID tacheId) {
        RemoveTacheFromTemplateGroupeTacheCommand command = new RemoveTacheFromTemplateGroupeTacheCommand(id, tacheId);
        TemplateGroupeTacheDTO groupe = removeTacheFromTemplateGroupeTacheApi.removeTacheFromGroupeTache(command);
        return ResponseEntity.ok(groupe);
    }
    
    @PutMapping("/{id}/reorder-taches")
    public ResponseEntity<TemplateGroupeTacheDTO> reorderTachesInTemplateGroupe(
            @PathVariable UUID id,
            @RequestBody ReorderTacheInGroupeCommand command) {
        ReorderTacheInGroupeCommand commandWithId = new ReorderTacheInGroupeCommand(id, command.orderdTacheId());
        TemplateGroupeTacheDTO groupe = reorderTachesInTemplateGroupeApi.reoderTacheInTemplateGroupe(commandWithId);
        return ResponseEntity.ok(groupe);
    }
}
