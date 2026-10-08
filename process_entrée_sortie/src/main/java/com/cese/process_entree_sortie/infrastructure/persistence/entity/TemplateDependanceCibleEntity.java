package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_dependance_cibles")
public class TemplateDependanceCibleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependance_id", nullable = false)
    private TemplateDependanceEntity dependance;
    
    @Column(name = "cible_tache_id", nullable = false, columnDefinition = "UUID")
    private UUID cibleTacheId;
    
    public TemplateDependanceCibleEntity() {}
    
    public TemplateDependanceCibleEntity(TemplateDependanceEntity dependance, UUID cibleTacheId) {
        this.dependance = dependance;
        this.cibleTacheId = cibleTacheId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public TemplateDependanceEntity getDependance() {
        return dependance;
    }
    
    public void setDependance(TemplateDependanceEntity dependance) {
        this.dependance = dependance;
    }
    
    public UUID getCibleTacheId() {
        return cibleTacheId;
    }
    
    public void setCibleTacheId(UUID cibleTacheId) {
        this.cibleTacheId = cibleTacheId;
    }
}
