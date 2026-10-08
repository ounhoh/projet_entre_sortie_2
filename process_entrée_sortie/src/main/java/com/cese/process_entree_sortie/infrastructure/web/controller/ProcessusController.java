package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.processus.entree.CreateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.processus.entree.UpdateEcheanceCommand;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessDetailDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreFiltreDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.processus.CancelProcessusApi;
import com.cese.process_entree_sortie.application.port.in.processus.CompleteProcessusApi;
import com.cese.process_entree_sortie.application.port.in.processus.CreateProcessusFromTemplateApi;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusByAgentApi;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusByIdApi;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusArbreApi;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusDetailsApi;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusEnRetardApi;
import com.cese.process_entree_sortie.application.port.in.processus.ListProcessusActifsApi;
import com.cese.process_entree_sortie.application.port.in.processus.ListProcessusByStatutApi;
import com.cese.process_entree_sortie.application.port.in.processus.StartProcessusByIdApi;
import com.cese.process_entree_sortie.application.port.in.processus.UpdateProcessusEcheanceApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processus")
public class ProcessusController {
    
    private final CreateProcessusFromTemplateApi createProcessusFromTemplateApi;
    private final StartProcessusByIdApi startProcessusByIdApi;
    private final CancelProcessusApi cancelProcessusApi;
    private final CompleteProcessusApi completeProcessusApi;
    private final UpdateProcessusEcheanceApi updateProcessusEcheanceApi;
    private final GetProcessusByIdApi getProcessusByIdApi;
    private final GetProcessusByAgentApi getProcessusByAgentApi;
    private final ListProcessusActifsApi listProcessusActifsApi;
    private final ListProcessusByStatutApi listProcessusByStatutApi;
    private final GetProcessusDetailsApi getProcessusDetailsApi;
    private final GetProcessusEnRetardApi getProcessusEnRetardApi;
    private final GetProcessusArbreApi getProcessusArbreApi;
    
    public ProcessusController(
            CreateProcessusFromTemplateApi createProcessusFromTemplateApi,
            StartProcessusByIdApi startProcessusByIdApi,
            CancelProcessusApi cancelProcessusApi,
            CompleteProcessusApi completeProcessusApi,
            UpdateProcessusEcheanceApi updateProcessusEcheanceApi,
            GetProcessusByIdApi getProcessusByIdApi,
            GetProcessusByAgentApi getProcessusByAgentApi,
            ListProcessusActifsApi listProcessusActifsApi,
            ListProcessusByStatutApi listProcessusByStatutApi,
            GetProcessusDetailsApi getProcessusDetailsApi,
            GetProcessusEnRetardApi getProcessusEnRetardApi,
            GetProcessusArbreApi getProcessusArbreApi) {
        this.createProcessusFromTemplateApi = createProcessusFromTemplateApi;
        this.startProcessusByIdApi = startProcessusByIdApi;
        this.cancelProcessusApi = cancelProcessusApi;
        this.completeProcessusApi = completeProcessusApi;
        this.updateProcessusEcheanceApi = updateProcessusEcheanceApi;
        this.getProcessusByIdApi = getProcessusByIdApi;
        this.getProcessusByAgentApi = getProcessusByAgentApi;
        this.listProcessusActifsApi = listProcessusActifsApi;
        this.listProcessusByStatutApi = listProcessusByStatutApi;
        this.getProcessusDetailsApi = getProcessusDetailsApi;
        this.getProcessusEnRetardApi = getProcessusEnRetardApi;
        this.getProcessusArbreApi = getProcessusArbreApi;
    }
    
    @PostMapping
    public ResponseEntity<ProcessusDTO> createProcessusFromTemplate(@RequestBody CreateProcessusCommand command) {
        ProcessusDTO processus = createProcessusFromTemplateApi.createProcessu(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(processus);
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<ProcessusDTO> startProcessus(@PathVariable UUID id) {
        ProcessusDTO processus = startProcessusByIdApi.startProcessusById(id);
        return ResponseEntity.ok(processus);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ProcessusDTO> cancelProcessus(@PathVariable UUID id) {
        ProcessusDTO processus = cancelProcessusApi.cancelProcessus(id);
        return ResponseEntity.ok(processus);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<ProcessusDTO> completeProcessus(@PathVariable UUID id) {
        ProcessusDTO processus = completeProcessusApi.completeProcessus(id);
        return ResponseEntity.ok(processus);
    }
    
    @PutMapping("/{id}/echeance")
    public ResponseEntity<ProcessusDTO> updateEcheance(
            @PathVariable UUID id,
            @RequestBody UpdateEcheanceCommand command) {
        UpdateEcheanceCommand commandWithId = new UpdateEcheanceCommand(id, command.nouvelleEcheance());
        ProcessusDTO processus = updateProcessusEcheanceApi.updateProcessusEcheance(commandWithId);
        return ResponseEntity.ok(processus);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProcessusDTO> getProcessusById(@PathVariable UUID id) {
        ProcessusDTO processus = getProcessusByIdApi.getProcessById(id);
        return ResponseEntity.ok(processus);
    }
    
    @GetMapping("/{id}/details")
    public ResponseEntity<ProcessDetailDTO> getProcessusDetails(@PathVariable UUID id) {
        ProcessDetailDTO details = getProcessusDetailsApi.getProcessusDetail(id);
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/{id}/arbre")
    public ResponseEntity<ProcessusArbreDTO> getProcessusArbre(
            @PathVariable UUID id,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) UUID agentId) {
        
        if (statut != null || agentId != null) {
            // Utiliser la version filtrée
            ProcessusArbreFiltreDTO.StatutFiltre statutFiltre = 
                statut != null 
                    ? ProcessusArbreFiltreDTO.StatutFiltre.valueOf(statut.toUpperCase())
                    : ProcessusArbreFiltreDTO.StatutFiltre.TOUS;
            
            ProcessusArbreFiltreDTO arbreFiltre = getProcessusArbreApi.getProcessusArbreFiltre(id, statutFiltre, agentId);
            // Convertir en ProcessusArbreDTO pour compatibilité
            ProcessusArbreDTO arbre = new ProcessusArbreDTO(arbreFiltre.processInfo(), arbreFiltre.racines());
            return ResponseEntity.ok(arbre);
        }
        
        ProcessusArbreDTO arbre = getProcessusArbreApi.getProcessusArbre(id);
        return ResponseEntity.ok(arbre);
    }
    
    @GetMapping("/{id}/arbre/filtre")
    public ResponseEntity<ProcessusArbreFiltreDTO> getProcessusArbreFiltre(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "TOUS") String statut,
            @RequestParam(required = false) UUID agentId) {
        
        ProcessusArbreFiltreDTO.StatutFiltre statutFiltre = 
            ProcessusArbreFiltreDTO.StatutFiltre.valueOf(statut.toUpperCase());
        
        ProcessusArbreFiltreDTO arbre = getProcessusArbreApi.getProcessusArbreFiltre(id, statutFiltre, agentId);
        return ResponseEntity.ok(arbre);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ProcessusDTO>> getProcessusByAgent(@PathVariable UUID agentId) {
        List<ProcessusDTO> processus = getProcessusByAgentApi.getProcessusByAgent(agentId);
        return ResponseEntity.ok(processus);
    }
    
    @GetMapping("/actifs")
    public ResponseEntity<List<ProcessusDTO>> listProcessusActifs() {
        List<ProcessusDTO> processus = listProcessusActifsApi.getListProcessusActifs();
        return ResponseEntity.ok(processus);
    }
    
    @GetMapping("/statut")
    public ResponseEntity<List<ProcessusDTO>> listProcessusByStatut(@RequestBody com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO statut) {
        List<ProcessusDTO> processus = listProcessusByStatutApi.getListProcessusByStatut(statut);
        return ResponseEntity.ok(processus);
    }
    
    @GetMapping("/en-retard")
    public ResponseEntity<List<ProcessusDTO>> getProcessusEnRetard() {
        List<ProcessusDTO> processus = getProcessusEnRetardApi.getProcessusEnRetard();
        return ResponseEntity.ok(processus);
    }
}
