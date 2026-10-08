package com.cese.process_entree_sortie.application.dto.processus.sortie;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessusDTO(UUID id, String code, StatutProcessusDTO statut, String labelleDirectionConcernee,
                           AgentDTO agent, TypeProcessus typeProcessus, LocalDate dateMobilite,
                           LocalDateTime dateArrivee, LocalDate dateEcheance, UUID templateId) {
}
