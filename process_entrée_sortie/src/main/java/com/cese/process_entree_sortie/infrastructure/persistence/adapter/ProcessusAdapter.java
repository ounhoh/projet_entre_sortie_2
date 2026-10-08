package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceProcessusEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.InstanceProcessusJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class ProcessusAdapter implements ProcessusSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessusAdapter.class);
    
    private final InstanceProcessusJpaRepository jpaRepository;
    private final GroupeTacheSpi groupeTacheSpi;
    private final DependanceSpi dependanceSpi;
    
    public ProcessusAdapter(InstanceProcessusJpaRepository jpaRepository,
                           GroupeTacheSpi groupeTacheSpi,
                           DependanceSpi dependanceSpi) {
        this.jpaRepository = jpaRepository;
        this.groupeTacheSpi = groupeTacheSpi;
        this.dependanceSpi = dependanceSpi;
    }
    
    @Override
    public InstanceProcessus save(InstanceProcessus processus) {
        InstanceProcessusEntity entity = convertirEnEntity(processus);
        InstanceProcessusEntity entitySauvegardee = jpaRepository.save(entity);
        
        // Sauvegarder les groupes de tâches et dépendances
        if (processus.groupeTachesList() != null) {
            processus.groupeTachesList().forEach(groupeTacheSpi::save);
        }
        if (processus.dependances() != null) {
            processus.dependances().forEach(dependanceSpi::save);
        }
        
        return chargerComplet(entitySauvegardee.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceProcessus> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(e -> chargerComplet(e.getId()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceProcessus> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceProcessus> findByStatut(StatutProcessus statutProcessus) {
        return jpaRepository.findByStatutId(statutProcessus.id()).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existByAgentIdAndStatut(UUID agentId, StatutProcessus statutProcessus) {
        return jpaRepository.existsByAgentIdAndStatutId(agentId, statutProcessus.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceProcessus> findActifs() {
        List<InstanceProcessusEntity> entities = jpaRepository.findActifs();
        logger.debug("Nombre de processus actifs trouvés dans la base: {}", entities.size());
        if (entities.isEmpty()) {
            logger.warn("Aucun processus actif trouvé. Vérifiez que les statuts ont bien codeStatut IN ('en_cours', 'en_attente')");
        }
        return entities.stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceProcessus> findEnRetards() {
        return jpaRepository.findEnRetards(LocalDate.now()).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceProcessus> findByDirectionId(UUID directionId) {
        return jpaRepository.findByDirectionConcerneeId(directionId).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(InstanceProcessus instanceProcessus) {
        jpaRepository.deleteById(instanceProcessus.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByStatut(StatutProcessus statutProcessus) {
        return jpaRepository.countByStatutId(statutProcessus.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByType(TypeProcessus typeProcessus) {
        return jpaRepository.countByType(typeProcessus);
    }
    
    private InstanceProcessusEntity convertirEnEntity(InstanceProcessus processus) {
        return new InstanceProcessusEntity(
            processus.id(),
            processus.codeProcessus(),
            processus.dateCreation(),
            processus.dateEcheance(),
            processus.directionConcerneeId(),
            processus.agentId(),
            processus.templateId(),
            processus.statutId(),
            processus.typeProcessus()
        );
    }
    
    private InstanceProcessus chargerComplet(UUID processusId) {
        InstanceProcessusEntity entity = jpaRepository.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé"));
        
        List<InstanceGroupeTache> groupeTaches = groupeTacheSpi.findByProcessusId(processusId);
        List<InstanceDependance> dependances = dependanceSpi.findByProcessusId(processusId);
        
        return new InstanceProcessus(
            entity.getId(),
            entity.getCodeProcessus(),
            entity.getDateCreation(),
            entity.getDateEcheance(),
            entity.getDirectionConcerneeId(),
            entity.getAgentId(),
            entity.getTemplateId(),
            entity.getStatutId(),
            groupeTaches,
            dependances,
            entity.getTypeProcessus()
        );
    }
}