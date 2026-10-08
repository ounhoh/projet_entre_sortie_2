package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groupes-taches")
public class GroupeTacheController {
    
    private final GetGroupeTacheByIdApi getGroupeTacheByIdApi;
    private final StartGroupeTacheApi startGroupeTacheApi;
    private final CompleteGroupeTacheApi completeGroupeTacheApi;
    private final ListGroupesByProcessusApi listGroupesByProcessusApi;
    private final GetGroupePretADemarrerApi getGroupePretADemarrerApi;
    private final GetGroupeBloqueeApi getGroupeBloqueeApi;
    
    public GroupeTacheController(
            GetGroupeTacheByIdApi getGroupeTacheByIdApi,
            StartGroupeTacheApi startGroupeTacheApi,
            CompleteGroupeTacheApi completeGroupeTacheApi,
            ListGroupesByProcessusApi listGroupesByProcessusApi,
            GetGroupePretADemarrerApi getGroupePretADemarrerApi,
            GetGroupeBloqueeApi getGroupeBloqueeApi) {
        this.getGroupeTacheByIdApi = getGroupeTacheByIdApi;
        this.startGroupeTacheApi = startGroupeTacheApi;
        this.completeGroupeTacheApi = completeGroupeTacheApi;
        this.listGroupesByProcessusApi = listGroupesByProcessusApi;
        this.getGroupePretADemarrerApi = getGroupePretADemarrerApi;
        this.getGroupeBloqueeApi = getGroupeBloqueeApi;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GroupeTacheDTO> getGroupeTacheById(@PathVariable UUID id) {
        GroupeTacheDTO groupe = getGroupeTacheByIdApi.getGroupeTacheById(id);
        return ResponseEntity.ok(groupe);
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<GroupeTacheDTO> startGroupeTache(@PathVariable UUID id) {
        GroupeTacheDTO groupe = startGroupeTacheApi.startGroupeTache(id);
        return ResponseEntity.ok(groupe);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<GroupeTacheDTO> completeGroupeTache(@PathVariable UUID id) {
        GroupeTacheDTO groupe = completeGroupeTacheApi.completeGroupeTacheApi(id);
        return ResponseEntity.ok(groupe);
    }
    
    @GetMapping("/processus/{processusId}")
    public ResponseEntity<List<GroupeTacheDTO>> listGroupesByProcessus(@PathVariable UUID processusId) {
        List<GroupeTacheDTO> groupes = listGroupesByProcessusApi.getListGroupesByProcessus(processusId);
        return ResponseEntity.ok(groupes);
    }
    
    @GetMapping("/processus/{processusId}/pret-a-demarrer")
    public ResponseEntity<List<GroupeTacheDTO>> getGroupePretADemarrer(@PathVariable UUID processusId) {
        List<GroupeTacheDTO> groupes = getGroupePretADemarrerApi.getGroupePretADemarrer(processusId);
        return ResponseEntity.ok(groupes);
    }
    
    @GetMapping("/processus/{processusId}/bloques")
    public ResponseEntity<List<GroupeTacheDTO>> getGroupeBloquee(@PathVariable UUID processusId) {
        List<GroupeTacheDTO> groupes = getGroupeBloqueeApi.getGroupeBloquee(processusId);
        return ResponseEntity.ok(groupes);
    }
}
