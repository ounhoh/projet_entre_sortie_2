package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

import java.util.UUID;

// passer d'agent dans le process d'entrée à agent actif
public interface ActiverAffectationApi {
    AgentAffectationDTO activerAffectation(UUID affectationId);
}
