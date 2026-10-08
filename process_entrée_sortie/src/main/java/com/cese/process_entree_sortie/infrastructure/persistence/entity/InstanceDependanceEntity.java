package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instance_dependance")
public class InstanceDependanceEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "source_tache_id", nullable = false, columnDefinition = "UUID")
    private UUID sourceTacheId;
    
    @OneToMany(mappedBy = "dependance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstanceDependanceCibleEntity> cibles = new ArrayList<>();
    
    @Column(name = "template_id", nullable = false, columnDefinition = "UUID")
    private UUID templateId;
    
    public InstanceDependanceEntity() {}
    
    public InstanceDependanceEntity(UUID id, UUID sourceTacheId, UUID templateId) {
        this.id = id;
        this.sourceTacheId = sourceTacheId;
        this.templateId = templateId;
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
    
    public List<InstanceDependanceCibleEntity> getCibles() {
        return cibles;
    }
    
    public void setCibles(List<InstanceDependanceCibleEntity> cibles) {
        this.cibles = cibles;
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }
}