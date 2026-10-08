package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "instance_dependance_cibles")
public class InstanceDependanceCibleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependance_id", nullable = false)
    private InstanceDependanceEntity dependance;
    
    @Column(name = "cible_tache_id", nullable = false, columnDefinition = "UUID")
    private UUID cibleTacheId;
    
    public InstanceDependanceCibleEntity() {}
    
    public InstanceDependanceCibleEntity(InstanceDependanceEntity dependance, UUID cibleTacheId) {
        this.dependance = dependance;
        this.cibleTacheId = cibleTacheId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public InstanceDependanceEntity getDependance() {
        return dependance;
    }
    
    public void setDependance(InstanceDependanceEntity dependance) {
        this.dependance = dependance;
    }
    
    public UUID getCibleTacheId() {
        return cibleTacheId;
    }
    
    public void setCibleTacheId(UUID cibleTacheId) {
        this.cibleTacheId = cibleTacheId;
    }
}
