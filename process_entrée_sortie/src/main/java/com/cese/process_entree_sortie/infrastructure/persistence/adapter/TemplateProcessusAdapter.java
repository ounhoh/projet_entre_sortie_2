package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.StatutProcessusEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateProcessusEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.StatutProcessusJpaRepository;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateProcessusJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Repository
@Transactional
public class TemplateProcessusAdapter implements TemplateProcessusSpi {
    
    private final TemplateProcessusJpaRepository jpaRepository;
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateDependanceSpi templateDependanceSpi;
    private final StatutProcessusJpaRepository statutProcessusJpaRepository;
    
    public TemplateProcessusAdapter(TemplateProcessusJpaRepository jpaRepository,
                                   TemplateGroupeTacheSpi templateGroupeTacheSpi,
                                   TemplateDependanceSpi templateDependanceSpi,
                                   StatutProcessusJpaRepository statutProcessusJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.templateDependanceSpi = templateDependanceSpi;
        this.statutProcessusJpaRepository = statutProcessusJpaRepository;
    }
    
    @Override
    public TemplateProcessus save(TemplateProcessus templateProcessus) {
        TemplateProcessusEntity entity = convertirEnEntity(templateProcessus);
        TemplateProcessusEntity entitySauvegardee = jpaRepository.save(entity);
        
        // Sauvegarder les groupes et dépendances via leurs SPI respectifs
        if (templateProcessus.groupeTacheList() != null) {
            templateProcessus.groupeTacheList().forEach(templateGroupeTacheSpi::save);
        }
        if (templateProcessus.dependanceList() != null) {
            templateProcessus.dependanceList().forEach(templateDependanceSpi::save);
        }
        
        return chargerComplet(entitySauvegardee.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateProcessus> findById(UUID templateId) {
        return jpaRepository.findById(templateId)
            .map(e -> chargerComplet(e.getId()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateProcessus> findAll() {
        return jpaRepository.findAll().stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateProcessus> findActifs() {
        return jpaRepository.findActifs().stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateProcessus> findByType(String type) {
        // Convertir le String en enum TypeProcessus
        TypeProcessus typeEnum;
        try {
            typeEnum = TypeProcessus.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type de processus invalide: " + type, e);
        }
        return jpaRepository.findByType(typeEnum).stream()
            .map(e -> chargerComplet(e.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TemplateProcessus templateProcessus) {
        jpaRepository.deleteById(templateProcessus.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }
    
    private TemplateProcessusEntity convertirEnEntity(TemplateProcessus template) {
        return new TemplateProcessusEntity(
            template.id(),
            template.codeProcessus(),
            template.libProcessus(),
            template.description(),
            template.type(),
            template.actif()
        );
    }
    
    private TemplateProcessus chargerComplet(UUID templateId) {
        TemplateProcessusEntity entity = jpaRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé"));
        
        List<TemplateGroupeTache> groupeTaches = templateGroupeTacheSpi.findByTemplateProcessusId(templateId);
        List<TemplateDependance> dependances = templateDependanceSpi.findByTemplateProcessusId(templateId);
        
        // Charger les statuts triés par ordre
        List<StatutProcessus> statuts = statutProcessusJpaRepository.findByTemplateProcessusId(templateId)
            .stream()
            .map(this::convertirEnStatutProcessus)
            .collect(Collectors.toList());
        
        return new TemplateProcessus(
            entity.getId(),
            entity.getCodeProcessus(),
            entity.getLibProcessus(),
            entity.getDescription(),
            entity.getType(),
            entity.getActif(),
            groupeTaches,
            statuts,
            dependances
        );
    }
    
    private StatutProcessus convertirEnStatutProcessus(StatutProcessusEntity entity) {
        return new StatutProcessus(
            entity.getId(),
            entity.getCodeStatut(),
            entity.getLibStatut()
        );
    }
}