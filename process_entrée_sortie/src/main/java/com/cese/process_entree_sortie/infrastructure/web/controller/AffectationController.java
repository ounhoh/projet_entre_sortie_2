package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerAgentAcceuilCommand;
import com.cese.process_entree_sortie.application.dto.affectation.entree.CreateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.entree.UpdateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.ActiverAffectationApi;
import com.cese.process_entree_sortie.application.port.in.affectation.AgentResponsableApi;
import com.cese.process_entree_sortie.application.port.in.affectation.AssignerAgentAcceuil;
import com.cese.process_entree_sortie.application.port.in.affectation.CreateAffectationApi;
import com.cese.process_entree_sortie.application.port.in.affectation.GetAffectationActiveByAgentApi;
import com.cese.process_entree_sortie.application.port.in.affectation.GetAffectationByIdApi;
import com.cese.process_entree_sortie.application.port.in.affectation.GetHistoriqueAffectationsApi;
import com.cese.process_entree_sortie.application.port.in.affectation.TerminerAffectationApi;
import com.cese.process_entree_sortie.application.port.in.affectation.UpdateAffectationApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/affectations")
public class AffectationController {
    
    private final CreateAffectationApi createAffectationApi;
    private final UpdateAffectationApi updateAffectationApi;
    private final GetAffectationByIdApi getAffectationByIdApi;
    private final GetAffectationActiveByAgentApi getAffectationActiveByAgentApi;
    private final GetHistoriqueAffectationsApi getHistoriqueAffectationsApi;
    private final TerminerAffectationApi terminerAffectationApi;
    private final ActiverAffectationApi activerAffectationApi;
    private final AgentResponsableApi agentResponsableApi;
    private final AssignerAgentAcceuil assignerAgentAcceuil;
    
    public AffectationController(
            CreateAffectationApi createAffectationApi,
            UpdateAffectationApi updateAffectationApi,
            GetAffectationByIdApi getAffectationByIdApi,
            GetAffectationActiveByAgentApi getAffectationActiveByAgentApi,
            GetHistoriqueAffectationsApi getHistoriqueAffectationsApi,
            TerminerAffectationApi terminerAffectationApi,
            ActiverAffectationApi activerAffectationApi,
            AgentResponsableApi agentResponsableApi,
            AssignerAgentAcceuil assignerAgentAcceuil) {
        this.createAffectationApi = createAffectationApi;
        this.updateAffectationApi = updateAffectationApi;
        this.getAffectationByIdApi = getAffectationByIdApi;
        this.getAffectationActiveByAgentApi = getAffectationActiveByAgentApi;
        this.getHistoriqueAffectationsApi = getHistoriqueAffectationsApi;
        this.terminerAffectationApi = terminerAffectationApi;
        this.activerAffectationApi = activerAffectationApi;
        this.agentResponsableApi = agentResponsableApi;
        this.assignerAgentAcceuil = assignerAgentAcceuil;
    }
    
    @PostMapping
    public ResponseEntity<AgentAffectationDTO> createAffectation(@RequestBody CreateAffectationCommand command) {
        AgentAffectationDTO affectation = createAffectationApi.createAffectation(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(affectation);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AgentAffectationDTO> updateAffectation(
            @PathVariable UUID id,
            @RequestBody UpdateAffectationCommand command) {
        AgentAffectationDTO affectation = updateAffectationApi.updateAffectation(command);
        return ResponseEntity.ok(affectation);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AgentAffectationDTO> getAffectationById(@PathVariable UUID id) {
        AgentAffectationDTO affectation = getAffectationByIdApi.getAffectationById(id);
        return ResponseEntity.ok(affectation);
    }
    
    @GetMapping("/agent/{agentId}/active")
    public ResponseEntity<AgentAffectationDTO> getAffectationActiveByAgent(@PathVariable UUID agentId) {
        AgentAffectationDTO affectation = getAffectationActiveByAgentApi.getAffectationAgent(agentId);
        return ResponseEntity.ok(affectation);
    }
    
    @GetMapping("/agent/{agentId}/historique")
    public ResponseEntity<List<AgentAffectationDTO>> getHistoriqueAffectations(@PathVariable UUID agentId) {
        List<AgentAffectationDTO> affectations = getHistoriqueAffectationsApi.getHistoriqueAffectation(agentId);
        return ResponseEntity.ok(affectations);
    }
    
    @PutMapping("/{id}/terminer")
    public ResponseEntity<AgentAffectationDTO> terminerAffectation(
            @PathVariable UUID id,
            @RequestBody java.time.LocalDate dateFin) {
        com.cese.process_entree_sortie.application.dto.affectation.entree.TerminerAffectationCommand command = 
            new com.cese.process_entree_sortie.application.dto.affectation.entree.TerminerAffectationCommand(id, dateFin);
        AgentAffectationDTO affectation = terminerAffectationApi.terminerAffectation(command);
        return ResponseEntity.ok(affectation);
    }
    
    @PutMapping("/{id}/activer")
    public ResponseEntity<AgentAffectationDTO> activerAffectation(@PathVariable UUID id) {
        AgentAffectationDTO affectation = activerAffectationApi.activerAffectation(id);
        return ResponseEntity.ok(affectation);
    }
    
    @PutMapping("/{id}/agent-responsable")
    public ResponseEntity<AgentAffectationDTO> assignerAgentResponsable(
            @PathVariable UUID id,
            @RequestBody UUID agentResponsableId) {
        com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerResponsableCommand command = 
            new com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerResponsableCommand(id, agentResponsableId);
        AgentAffectationDTO affectation = agentResponsableApi.assignerResponsable(command);
        return ResponseEntity.ok(affectation);
    }
    
    @PutMapping("/{id}/agent-acceuil")
    public ResponseEntity<AgentAffectationDTO> assignerAgentAcceuil(
            @PathVariable UUID id,
            @RequestBody AssignerAgentAcceuilCommand command) {
        AgentAffectationDTO affectation = assignerAgentAcceuil.assignerAgentAcceuil(command);
        return ResponseEntity.ok(affectation);
    }
}
