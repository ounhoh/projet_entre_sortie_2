package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateGroupeTacheAssociationEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateGroupeTacheAssociationJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroupeTacheConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupeTacheConverter.class);
    
    private final TacheConverter tacheConverter;
    private final TacheSpi tacheSpi;
    private final TemplateGroupeTacheAssociationJpaRepository associationRepository;
    
    public GroupeTacheConverter(
            TacheConverter tacheConverter, 
            TacheSpi tacheSpi,
            TemplateGroupeTacheAssociationJpaRepository associationRepository) {
        this.tacheConverter = tacheConverter;
        this.tacheSpi = tacheSpi;
        this.associationRepository = associationRepository;
    }
    
    public GroupeTacheDTO convertirEnDTO(InstanceGroupeTache groupeTache) {
        // Récupérer les tâches du groupe
        List<InstanceTache> taches = tacheSpi.findByGroupeId(groupeTache.id());
        
        // Récupérer l'ordre des tâches depuis le template
        List<TemplateGroupeTacheAssociationEntity> associations = 
            associationRepository.findByTemplateGroupeId(groupeTache.templateId());
        
        // DEBUG: Logger pour vérifier les associations récupérées
        logger.info("Groupe {} (templateId: {}): {} associations trouvées", 
            groupeTache.id(), groupeTache.templateId(), associations.size());
        associations.forEach(a -> logger.info("  - Tâche template {}: ordre {}", 
            a.getTemplateTacheId(), a.getOrdre()));
        
        // Créer un map templateTacheId -> ordre
        Map<UUID, Integer> ordreMap = associations.stream()
            .filter(a -> a.getOrdre() != null)
            .collect(Collectors.toMap(
                TemplateGroupeTacheAssociationEntity::getTemplateTacheId,
                TemplateGroupeTacheAssociationEntity::getOrdre
            ));
        
        // DEBUG: Logger pour vérifier l'ordre map et les tâches
        logger.info("Ordre map créé avec {} entrées pour le groupe {}", ordreMap.size(), groupeTache.id());
        taches.forEach(t -> {
            Integer ordre = ordreMap.get(t.templateId());
            logger.info("  - Tâche {} (templateId: {}, code: {}): ordre {}", 
                t.id(), t.templateId(), t.code(), ordre != null ? ordre : "NON DÉFINI");
        });
        
        // Trier les tâches par ordre (si disponible), sinon par templateId puis code
        // Les tâches avec ordre sont triées en premier selon leur ordre, 
        // puis celles sans ordre sont triées par templateId puis code
        List<TacheDTO> tacheDTOs = taches.stream()
            .sorted(Comparator
                .comparing((InstanceTache t) -> {
                    Integer ordre = ordreMap.get(t.templateId());
                    // Si pas d'ordre défini, utiliser Integer.MAX_VALUE pour les mettre après
                    return ordre != null ? ordre : Integer.MAX_VALUE;
                })
                .thenComparing(InstanceTache::templateId)
                .thenComparing(InstanceTache::code))
            .map(tacheConverter::convertirEnDTO)
            .collect(Collectors.toList());
        
        // DEBUG: Logger pour vérifier l'ordre final
        logger.info("Tâches triées pour le groupe {}:", groupeTache.id());
        // Créer un map pour retrouver rapidement les tâches originales
        Map<UUID, InstanceTache> tachesMap = taches.stream()
            .collect(Collectors.toMap(InstanceTache::id, t -> t));
        for (int i = 0; i < tacheDTOs.size(); i++) {
            TacheDTO t = tacheDTOs.get(i);
            InstanceTache tacheOriginale = tachesMap.get(t.id());
            Integer ordre = tacheOriginale != null ? ordreMap.get(tacheOriginale.templateId()) : null;
            logger.info("  Position {}: {} (ordre: {})", 
                i + 1, t.libelle(), ordre != null ? ordre : "NON DÉFINI");
        }
        
        return new GroupeTacheDTO(
            groupeTache.id(),
            groupeTache.code(),
            groupeTache.libelle(),
            groupeTache.statut(),
            groupeTache.dateEcheance(), // InstanceGroupeTache utilise dateEcheance
            tacheDTOs,
            groupeTache.templateId()
        );
    }
}
