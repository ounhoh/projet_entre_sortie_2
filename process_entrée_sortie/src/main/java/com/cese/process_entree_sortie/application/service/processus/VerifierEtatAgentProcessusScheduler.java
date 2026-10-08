package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VerifierEtatAgentProcessusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VerifierEtatAgentProcessusScheduler.class);

    private final ProcessusSpi processusSpi;
    private final TemplateProcessusSpi templateProcessusSpi;
    private final AgentSpi agentSpi;
    private final AgentDirectionSpi agentDirectionSpi;

    public VerifierEtatAgentProcessusScheduler(ProcessusSpi processusSpi,
                                               TemplateProcessusSpi templateProcessusSpi,
                                               AgentSpi agentSpi,
                                               AgentDirectionSpi agentDirectionSpi) {
        this.processusSpi = processusSpi;
        this.templateProcessusSpi = templateProcessusSpi;
        this.agentSpi = agentSpi;
        this.agentDirectionSpi = agentDirectionSpi;
    }

    // Toutes les 10 minutes (configurable via application.yaml)
    @Scheduled(fixedDelayString = "${processus.agentEtat.schedulerDelay:600000}")
    public void verifierEtMettreAJourEtatAgents() {
        List<InstanceProcessus> processusActifs = processusSpi.findActifs();
        logger.debug("Vérification périodique des états agents (processus actifs: {})", processusActifs.size());
        processusActifs.forEach(this::verifierEtMettreAJourEtatAgent);
    }

    private void verifierEtMettreAJourEtatAgent(InstanceProcessus processus) {
        try {
            TemplateProcessus template = templateProcessusSpi.findById(processus.templateId()).orElse(null);
            if (template == null || template.statusList() == null || template.statusList().isEmpty()) {
                logger.debug("Skip processus {}: template ou statusList indisponible", processus.id());
                return;
            }

            StatutProcessus statutActuel = template.statusList().stream()
                .filter(s -> s.id().equals(processus.statutId()))
                .findFirst()
                .orElse(null);
            if (statutActuel == null) {
                logger.debug("Skip processus {}: statutActuel introuvable (statutId={})", processus.id(), processus.statutId());
                return;
            }

            StatutProcessus statutArchive = template.statusList().stream()
                .filter(s -> "archive".equals(s.codeStatut()))
                .findFirst()
                .orElse(null);
            if (statutArchive != null && statutActuel.id().equals(statutArchive.id())) {
                logger.debug("Skip processus {}: déjà archivé", processus.id());
                return;
            }

            StatutProcessus dernierStatutFonctionnel = template.statusList().stream()
                .filter(s -> !"archive".equals(s.codeStatut()))
                .reduce((first, second) -> second)
                .orElse(null);
            if (dernierStatutFonctionnel == null || !statutActuel.id().equals(dernierStatutFonctionnel.id())) {
                logger.debug(
                    "Skip processus {}: pas au dernier statut fonctionnel (actuel={}, dernier={})",
                    processus.id(),
                    statutActuel.codeStatut(),
                    dernierStatutFonctionnel != null ? dernierStatutFonctionnel.codeStatut() : null
                );
                return;
            }

            LocalDate aujourdHui = LocalDate.now();
            LocalDate dateMobilite = null;
            if (processus.typeProcessus() == TypeProcessus.entree || processus.typeProcessus() == TypeProcessus.mobitliteInterne) {
                dateMobilite = agentDirectionSpi.findCurrentByAgentId(processus.agentId())
                    .map(AgentDirection::dateArrivee)
                    .orElse(null);
            } else if (processus.typeProcessus() == TypeProcessus.sortie) {
                dateMobilite = agentDirectionSpi.findByAgentId(processus.agentId())
                    .stream()
                    .filter(ad -> ad.dateDepart() != null)
                    .findFirst()
                    .map(AgentDirection::dateDepart)
                    .orElse(null);
            }

            if (dateMobilite == null || dateMobilite.isAfter(aujourdHui)) {
                logger.debug(
                    "Skip processus {}: dateMobilite invalide (dateMobilite={}, aujourdHui={})",
                    processus.id(),
                    dateMobilite,
                    aujourdHui
                );
                return;
            }

            AgentPersonnel agent = agentSpi.findById(processus.agentId()).orElse(null);
            if (agent == null) {
                logger.debug("Skip processus {}: agent introuvable (agentId={})", processus.id(), processus.agentId());
                return;
            }

            EtatAgent nouvelEtat = null;
            if (processus.typeProcessus() == TypeProcessus.entree && agent.etatAgent() == EtatAgent.entree) {
                nouvelEtat = EtatAgent.actif;
            } else if (processus.typeProcessus() == TypeProcessus.mobitliteInterne && agent.etatAgent() == EtatAgent.mobiliteInterne) {
                nouvelEtat = EtatAgent.actif;
            } else if (processus.typeProcessus() == TypeProcessus.sortie && agent.etatAgent() == EtatAgent.sortie) {
                nouvelEtat = EtatAgent.ancien;
            }

            if (nouvelEtat != null) {
                AgentPersonnel agentModifie = agent.changerEtatAgent(nouvelEtat);
                agentSpi.save(agentModifie);
                logger.info("Agent {} mis à jour au statut {} (processus {})", agent.id(), nouvelEtat, processus.id());
            } else {
                logger.debug(
                    "Skip processus {}: état agent incompatible (typeProcessus={}, etatAgent={})",
                    processus.id(),
                    processus.typeProcessus(),
                    agent.etatAgent()
                );
            }

            if (statutArchive != null) {
                InstanceProcessus processusArchive = processus.changerStatut(statutArchive.id());
                processusSpi.save(processusArchive);
                logger.info("Processus {} archivé (statut {})", processus.id(), statutArchive.libStatut());
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la mise à jour de l'état de l'agent pour le processus {}: {}", processus.id(), e.getMessage());
        }
    }
}
