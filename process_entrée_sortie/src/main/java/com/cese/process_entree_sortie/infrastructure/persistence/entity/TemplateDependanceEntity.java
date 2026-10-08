package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "template_dependance")
public class TemplateDependanceEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "source_tache_id", nullable = false, columnDefinition = "UUID")
    private UUID sourceTacheId;
    
    @OneToMany(mappedBy = "dependance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateDependanceCibleEntity> cibles = new ArrayList<>();
    
    public TemplateDependanceEntity() {}
    
    public TemplateDependanceEntity(UUID id, UUID sourceTacheId) {
        this.id = id;
        this.sourceTacheId = sourceTacheId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getSourceTacheId() {
        return sourceTacheId;
    }
    
    public void setSourceTacheId(UUID sourceTacheId) {
        this.sourceTacheId = sourceTacheId;
    }
    
    public List<TemplateDependanceCibleEntity> getCibles() {
        return cibles;
    }
    
    public void setCibles(List<TemplateDependanceCibleEntity> cibles) {
        this.cibles = cibles;
    }
}