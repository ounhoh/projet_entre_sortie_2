package com.cese.process_entree_sortie.application.dto.agent.sortie;

import com.cese.process_entree_sortie.domain.Agent.model.Role;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;

import java.time.LocalDate;
import java.util.UUID;

public record AgentDTO (UUID id, String nom, String prenom, String email, Role role,
                        String code, String libelle, String service, String direction,
                        UUID agentResponsable, LocalDate dateArrivee, LocalDate dateSortie, EtatAgent etatAgent,
                    String numeroBureau) {
}
