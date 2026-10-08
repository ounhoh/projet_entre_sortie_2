package com.cese.process_entree_sortie.application.service.dependance;

import com.cese.process_entree_sortie.application.dto.tache.entree.CreateDependanceCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.application.port.in.dependance.CreateDependanceApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateDependanceService implements CreateDependanceApi {
    
    private final DependanceSpi dependanceSpi;
    private final ProcessusSpi processusSpi;
    private final TacheSpi tacheSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    
    public CreateDependanceService(
            DependanceSpi dependanceSpi, 
            ProcessusSpi processusSpi,
            TacheSpi tacheSpi,
            GroupeTacheSpi groupeTacheSpi) {
        this.dependanceSpi = dependanceSpi;
        this.processusSpi = processusSpi;
        this.tacheSpi = tacheSpi;
        this.groupeTacheSpi = groupeTacheSpi;
    }
    
    @Override
    public TemplateDependanceDTO createDependance(CreateDependanceCommand command) {
        // Récupérer la tâche source pour obtenir le processus et le templateId
        InstanceTache tacheSource = tacheSpi.findById(command.sourceTacheId())
            .orElseThrow(() -> new RuntimeException("Tâche source non trouvée: " + command.sourceTacheId()));
        
        // Récupérer le groupe de la tâche source
        InstanceGroupeTache groupe = groupeTacheSpi.findById(tacheSource.groupeTacheId())
            .orElseThrow(() -> new RuntimeException("Groupe de tâche non trouvé: " + tacheSource.groupeTacheId()));
        
        // Récupérer le processus pour obtenir le templateId
        InstanceProcessus processus = processusSpi.findById(groupe.processusId())
            .orElseThrow(() -> new RuntimeException("Processus non trouvé: " + groupe.processusId()));
        
        // Vérifier que toutes les tâches cibles existent et sont dans le même processus
        for (UUID cibleTacheId : command.cibleTacheIds()) {
            InstanceTache tacheCible = tacheSpi.findById(cibleTacheId)
                .orElseThrow(() -> new RuntimeException("Tâche cible non trouvée: " + cibleTacheId));
            
            InstanceGroupeTache groupeCible = groupeTacheSpi.findById(tacheCible.groupeTacheId())
                .orElseThrow(() -> new RuntimeException("Groupe de tâche cible non trouvé: " + tacheCible.groupeTacheId()));
            
            if (!groupeCible.processusId().equals(processus.id())) {
                throw new RuntimeException("Les tâches doivent être dans le même processus");
            }
        }
        
        // Récupérer le templateId depuis le processus
        UUID templateId = processus.templateId();
        
        // Créer la dépendance avec la nouvelle structure
        InstanceDependance nouvelleDependance = new InstanceDependance(
            UUID.randomUUID(),
            command.sourceTacheId(),
            command.cibleTacheIds(),
            templateId
        );
        
        InstanceDependance dependanceSauvegarde = dependanceSpi.save(nouvelleDependance);
        
        // Mettre à jour les tâches cibles avec leur dependanceId
        for (UUID cibleTacheId : command.cibleTacheIds()) {
            InstanceTache tacheCible = tacheSpi.findById(cibleTacheId)
                .orElseThrow(() -> new RuntimeException("Tâche cible non trouvée: " + cibleTacheId));
            
            // Créer une nouvelle instance avec le dependanceId
            InstanceTache tacheAvecDependance = new InstanceTache(
                tacheCible.id(),
                tacheCible.code(),
                tacheCible.libelle(),
                tacheCible.contenu(),
                tacheCible.statut(),
                tacheCible.dateEcheance(),
                tacheCible.templateId(),
                tacheCible.groupeTacheId(),
                dependanceSauvegarde.id()
            );
            
            tacheSpi.save(tacheAvecDependance);
        }
        
        // Convertir en TemplateDependanceDTO (même structure que TemplateDependance)
        return new TemplateDependanceDTO(
            dependanceSauvegarde.id(),
            dependanceSauvegarde.sourceTacheId(),
            dependanceSauvegarde.cibleTacheIds()
        );
    }
}
