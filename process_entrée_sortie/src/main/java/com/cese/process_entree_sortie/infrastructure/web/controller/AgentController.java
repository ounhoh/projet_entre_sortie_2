package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerEmailCommand;
import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerRoleCommand;
import com.cese.process_entree_sortie.application.dto.agent.entree.CreateAgentCommand;
import com.cese.process_entree_sortie.application.dto.agent.entree.SearchAgentsCommand;
import com.cese.process_entree_sortie.application.dto.agent.entree.UpdateAgentCommandCommand;
import com.cese.process_entree_sortie.application.dto.agent.entree.ListAgentsQueryCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.ChangerAgentEmailApi;
import com.cese.process_entree_sortie.application.port.in.agent.ChangerRoleApi;
import com.cese.process_entree_sortie.application.port.in.agent.CreateAgentApi;
import com.cese.process_entree_sortie.application.port.in.agent.DeleteAgentApi;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentByCodeApi;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentByIdApi;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentDetail;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentDiffusionApi;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentMaterielApi;
import com.cese.process_entree_sortie.application.port.in.agent.ListAgentAPI;
import com.cese.process_entree_sortie.application.port.in.agent.SearchAgentsApi;
import com.cese.process_entree_sortie.application.port.in.agent.UpdateAgentApi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    private final CreateAgentApi createAgentApi;
    private final ListAgentAPI listAgentAPI;
    private final GetAgentByIdApi getAgentByIdApi;
    private final UpdateAgentApi updateAgentApi;
    private final DeleteAgentApi deleteAgentApi;
    private final GetAgentByCodeApi getAgentByCodeApi;
    private final ChangerRoleApi changerRoleApi;
    private final ChangerAgentEmailApi changerAgentEmailApi;
    private final SearchAgentsApi searchAgentsApi;
    private final GetAgentDetail getAgentDetail;
    private final GetAgentMaterielApi getAgentMaterielApi;
    private final GetAgentDiffusionApi getAgentDiffusionApi;
    
    public AgentController(
            CreateAgentApi createAgentApi,
            ListAgentAPI listAgentAPI,
            GetAgentByIdApi getAgentByIdApi,
            UpdateAgentApi updateAgentApi,
            DeleteAgentApi deleteAgentApi,
            GetAgentByCodeApi getAgentByCodeApi,
            ChangerRoleApi changerRoleApi,
            ChangerAgentEmailApi changerAgentEmailApi,
            SearchAgentsApi searchAgentsApi,
            GetAgentDetail getAgentDetail,
            GetAgentMaterielApi getAgentMaterielApi,
            GetAgentDiffusionApi getAgentDiffusionApi) {
        this.createAgentApi = createAgentApi;
        this.listAgentAPI = listAgentAPI;
        this.getAgentByIdApi = getAgentByIdApi;
        this.updateAgentApi = updateAgentApi;
        this.deleteAgentApi = deleteAgentApi;
        this.getAgentByCodeApi = getAgentByCodeApi;
        this.changerRoleApi = changerRoleApi;
        this.changerAgentEmailApi = changerAgentEmailApi;
        this.searchAgentsApi = searchAgentsApi;
        this.getAgentDetail = getAgentDetail;
        this.getAgentMaterielApi = getAgentMaterielApi;
        this.getAgentDiffusionApi = getAgentDiffusionApi;
    }
    
    @PostMapping
    public ResponseEntity<AgentDTO> createAgent(@RequestBody CreateAgentCommand command) {
        AgentDTO agent = createAgentApi.createAgent(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(agent);
    }
    
    @GetMapping
    public ResponseEntity<List<AgentDTO>> listAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        ListAgentsQueryCommand query = new ListAgentsQueryCommand(page, size, sortBy, direction);
        List<AgentDTO> agents = listAgentAPI.getsAgents(query);
        return ResponseEntity.ok(agents);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getAgentById(@PathVariable UUID id) {
        AgentDTO agent = getAgentByIdApi.getAgentById(id);
        return ResponseEntity.ok(agent);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<AgentDTO> getAgentByCode(@PathVariable String code) {
        AgentDTO agent = getAgentByCodeApi.getAgentByCode(code);
        return ResponseEntity.ok(agent);
    }
    
    @GetMapping("/{id}/detail")
    public ResponseEntity<AgentDTO> getAgentDetail(@PathVariable UUID id) {
        AgentDTO agent = getAgentDetail.getAgentDetail(id);
        return ResponseEntity.ok(agent);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> updateAgent(
            @PathVariable UUID id,
            @RequestBody UpdateAgentCommandCommand command) {
        UpdateAgentCommandCommand commandWithId = new UpdateAgentCommandCommand(id, command.nom(), command.prenom(), command.email(), command.role());
        AgentDTO agent = updateAgentApi.updateAgent(commandWithId);
        return ResponseEntity.ok(agent);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable UUID id) {
        deleteAgentApi.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<AgentDTO> changerRole(
            @PathVariable UUID id,
            @RequestBody ChangerRoleCommand command) {
        ChangerRoleCommand commandWithId = new ChangerRoleCommand(id, command.newRole());
        AgentDTO agent = changerRoleApi.changerRole(commandWithId);
        return ResponseEntity.ok(agent);
    }
    
    @PutMapping("/{id}/email")
    public ResponseEntity<AgentDTO> changerEmail(
            @PathVariable UUID id,
            @RequestBody ChangerEmailCommand command) {
        ChangerEmailCommand commandWithId = new ChangerEmailCommand(id, command.newEmail());
        AgentDTO agent = changerAgentEmailApi.changerAgentEmail(commandWithId);
        return ResponseEntity.ok(agent);
    }
    
    @PostMapping("/search")
    public ResponseEntity<AgentDTO> searchAgents(@RequestBody SearchAgentsCommand command) {
        AgentDTO agent = searchAgentsApi.searchAgents(command);
        return ResponseEntity.ok(agent);
    }
    
    @GetMapping("/{id}/materiel")
    public ResponseEntity<AgentMaterielEtDroit> getAgentMateriel(@PathVariable UUID id) {
        AgentMaterielEtDroit materiel = getAgentMaterielApi.getAgentMateriel(id)
            .orElseThrow(() -> new RuntimeException("Matériel non trouvé pour l'agent avec l'ID: " + id));
        return ResponseEntity.ok(materiel);
    }

    @GetMapping("/{id}/diffusion")
    public ResponseEntity<List<String>> getAgentDiffusion(@PathVariable UUID id) {
        List<String> diffusions = getAgentDiffusionApi.getAgentDiffusion(id);
        return ResponseEntity.ok(diffusions);
    }
}
