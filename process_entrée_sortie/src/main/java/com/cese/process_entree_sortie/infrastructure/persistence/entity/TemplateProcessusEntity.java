package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_processus")
public class TemplateProcessusEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code_processus", nullable = false, unique = true)
    private String codeProcessus;
    
    @Column(name = "lib_processus", nullable = false)
    private String libProcessus;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeProcessus type;
    
    @Column(name = "actif", nullable = false)
    private Boolean actif;
    
    public TemplateProcessusEntity() {}
    
    public TemplateProcessusEntity(UUID id, String codeProcessus, String libProcessus, 
                                   String description, TypeProcessus type, Boolean actif) {
        this.id = id;
        this.codeProcessus = codeProcessus;
        this.libProcessus = libProcessus;
        this.description = description;
        this.type = type;
        this.actif = actif;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCodeProcessus() {
        return codeProcessus;
    }
    
    public void setCodeProcessus(String codeProcessus) {
        this.codeProcessus = codeProcessus;
    }
    
    public String getLibProcessus() {
        return libProcessus;
    }
    
    public void setLibProcessus(String libProcessus) {
        this.libProcessus = libProcessus;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TypeProcessus getType() {
        return type;
    }
    
    public void setType(TypeProcessus type) {
        this.type = type;
    }
    
    public Boolean getActif() {
        return actif;
    }
    
    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}