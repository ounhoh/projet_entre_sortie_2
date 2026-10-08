package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.visualisation.sortie.AvancementDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.visualisation.AvancementApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/avancement")
public class AvancementController {
    
    private final AvancementApi avancementApi;
    
    public AvancementController(AvancementApi avancementApi) {
        this.avancementApi = avancementApi;
    }
    
    @GetMapping("/processus/{processusId}")
    public ResponseEntity<AvancementDTO> getAvancementProcessus(@PathVariable UUID processusId) {
        AvancementDTO avancement = avancementApi.getAvancementProcessus(processusId);
        return ResponseEntity.ok(avancement);
    }
    
    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<AvancementDTO> getAvancementGroupe(@PathVariable UUID groupeId) {
        AvancementDTO avancement = avancementApi.getAvancementGroupe(groupeId);
        return ResponseEntity.ok(avancement);
    }
}
