package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "direction")
public class DirectionEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code_direction", nullable = false, unique = true)
    private String codeDirection;
    
    @Column(name = "lib_direction", nullable = false)
    private String libDirection;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    public DirectionEntity() {}
    
    public DirectionEntity(UUID id, String codeDirection, String libDirection) {
        this.id = id;
        this.codeDirection = codeDirection;
        this.libDirection = libDirection;
    }
    
    public DirectionEntity(UUID id, String codeDirection, String libDirection, String description) {
        this.id = id;
        this.codeDirection = codeDirection;
        this.libDirection = libDirection;
        this.description = description;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCodeDirection() {
        return codeDirection;
    }
    
    public void setCodeDirection(String codeDirection) {
        this.codeDirection = codeDirection;
    }
    
    public String getLibDirection() {
        return libDirection;
    }
    
    public void setLibDirection(String libDirection) {
        this.libDirection = libDirection;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}