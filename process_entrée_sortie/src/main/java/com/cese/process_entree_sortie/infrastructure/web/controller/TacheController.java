package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.tache.entree.AssignerAgentToTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.entree.DesassignerAgentToTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.entree.SubmitFormulaireCommand;
import com.cese.process_entree_sortie.application.dto.tache.entree.UpdateEcheanceTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/taches")
public class TacheController {
    
    private final GetTacheByIdApi getTacheByIdApi;
    private final StartTacheApi startTacheApi;
    private final CompleteTacheApi completeTacheApi;
    private final ReopenTacheApi reopenTacheApi;
    private final AssignerAgentToTacheApi assignerAgentToTacheApi;
    private final DesassignerAgentToTacheApi desassignerAgentToTacheApi;
    private final UpdateDateEchanceApi updateDateEchanceApi;
    private final SubmitFormulaireApi submitFormulaireApi;
    private final ListTacheByProcessusApi listTacheByProcessusApi;
    private final ListeTacheAgentApi listeTacheAgentApi;
    private final GetMyTacheApi getMyTacheApi;
    private final ListTacheEnRetardApi listTacheEnRetardApi;
    private final GetTachePretADemarrerApi getTachePretADemarrerApi;
    private final GetTacheBloqueApi getTacheBloqueApi;
    private final AnnulerValidationTacheApi annulerValidationTacheApi;
    
    public TacheController(
            GetTacheByIdApi getTacheByIdApi,
            StartTacheApi startTacheApi,
            CompleteTacheApi completeTacheApi,
            ReopenTacheApi reopenTacheApi,
            AssignerAgentToTacheApi assignerAgentToTacheApi,
            DesassignerAgentToTacheApi desassignerAgentToTacheApi,
            UpdateDateEchanceApi updateDateEchanceApi,
            SubmitFormulaireApi submitFormulaireApi,
            ListTacheByProcessusApi listTacheByProcessusApi,
            ListeTacheAgentApi listeTacheAgentApi,
            GetMyTacheApi getMyTacheApi,
            ListTacheEnRetardApi listTacheEnRetardApi,
            GetTachePretADemarrerApi getTachePretADemarrerApi,
            GetTacheBloqueApi getTacheBloqueApi,
            AnnulerValidationTacheApi annulerValidationTacheApi) {
        this.getTacheByIdApi = getTacheByIdApi;
        this.startTacheApi = startTacheApi;
        this.completeTacheApi = completeTacheApi;
        this.reopenTacheApi = reopenTacheApi;
        this.assignerAgentToTacheApi = assignerAgentToTacheApi;
        this.desassignerAgentToTacheApi = desassignerAgentToTacheApi;
        this.updateDateEchanceApi = updateDateEchanceApi;
        this.submitFormulaireApi = submitFormulaireApi;
        this.listTacheByProcessusApi = listTacheByProcessusApi;
        this.listeTacheAgentApi = listeTacheAgentApi;
        this.getMyTacheApi = getMyTacheApi;
        this.listTacheEnRetardApi = listTacheEnRetardApi;
        this.getTachePretADemarrerApi = getTachePretADemarrerApi;
        this.getTacheBloqueApi = getTacheBloqueApi;
        this.annulerValidationTacheApi = annulerValidationTacheApi;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TacheDTO> getTacheById(@PathVariable UUID id) {
        TacheDTO tache = getTacheByIdApi.getTacheById(id);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<TacheDTO> startTache(@PathVariable UUID id) {
        TacheDTO tache = startTacheApi.startTache(id);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<TacheDTO> completeTache(@PathVariable UUID id) {
        TacheDTO tache = completeTacheApi.completeTache(id);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/annuler-validation")
    public ResponseEntity<TacheDTO> annulerValidationTache(@PathVariable UUID id) {
        TacheDTO tache = annulerValidationTacheApi.annulerValidationTache(id);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/reopen")
    public ResponseEntity<TacheDTO> reopenTache(@PathVariable UUID id) {
        TacheDTO tache = reopenTacheApi.reopenTache(id);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/assigner-agent")
    public ResponseEntity<TacheDTO> assignerAgentToTache(
            @PathVariable UUID id,
            @RequestBody AssignerAgentToTacheCommand command) {
        TacheDTO tache = assignerAgentToTacheApi.assignerAgentToTache(command);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/desassigner-agent")
    public ResponseEntity<TacheDTO> desassignerAgentToTache(
            @PathVariable UUID id,
            @RequestBody DesassignerAgentToTacheCommand command) {
        TacheDTO tache = desassignerAgentToTacheApi.desassignerAgenToTache(command);
        return ResponseEntity.ok(tache);
    }
    
    @PutMapping("/{id}/echeance")
    public ResponseEntity<TacheDTO> updateDateEcheance(
            @PathVariable UUID id,
            @RequestBody UpdateEcheanceTacheCommand command) {
        UpdateEcheanceTacheCommand commandWithId = new UpdateEcheanceTacheCommand(id, command.nouvelleEcheance());
        TacheDTO tache = updateDateEchanceApi.updateDateEcheance(commandWithId);
        return ResponseEntity.ok(tache);
    }
    
    @PostMapping("/{id}/submit-formulaire")
    public ResponseEntity<TacheDTO> submitFormulaire(
            @PathVariable UUID id,
            @RequestBody SubmitFormulaireCommand command) {
        TacheDTO tache = submitFormulaireApi.submitFormulaire(command);
        return ResponseEntity.ok(tache);
    }
    
    @GetMapping("/processus/{processusId}")
    public ResponseEntity<List<TacheDTO>> listTacheByProcessus(@PathVariable UUID processusId) {
        List<TacheDTO> taches = listTacheByProcessusApi.getListTacheByProcessus(processusId);
        return ResponseEntity.ok(taches);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TacheDTO>> listeTacheAgent(@PathVariable UUID agentId) {
        List<TacheDTO> taches = listeTacheAgentApi.getListTacheAgent(agentId);
        return ResponseEntity.ok(taches);
    }
    
    @GetMapping("/agent/{agentId}/my-taches")
    public ResponseEntity<List<TacheDTO>> getMyTache(@PathVariable UUID agentId) {
        List<TacheDTO> taches = getMyTacheApi.getMyTache(agentId);
        return ResponseEntity.ok(taches);
    }
    
    @GetMapping("/agent/{agentId}/en-retard")
    public ResponseEntity<List<TacheDTO>> listTacheEnRetard(@PathVariable UUID agentId) {
        List<TacheDTO> taches = listTacheEnRetardApi.getListTacheEnRetard(agentId);
        return ResponseEntity.ok(taches);
    }
    
    @GetMapping("/processus/{processusId}/pret-a-demarrer")
    public ResponseEntity<List<TacheDTO>> getTachePretADemarrer(@PathVariable UUID processusId) {
        List<TacheDTO> taches = getTachePretADemarrerApi.getTachePretADemarrer(processusId);
        return ResponseEntity.ok(taches);
    }
    
    @GetMapping("/processus/{processusId}/bloquees")
    public ResponseEntity<List<TacheDTO>> getTacheBloque(@PathVariable UUID processusId) {
        List<TacheDTO> taches = getTacheBloqueApi.getTacheBloque(processusId);
        return ResponseEntity.ok(taches);
    }
}
