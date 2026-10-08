package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "template_tache")
public class TemplateTacheEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code", nullable = false)
    private String code;
    
    @Column(name = "libelle", nullable = false)
    private String libelle;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TacheType type;
    
    @Column(name = "delai_jour", nullable = false)
    private int delaiJour;
    
    @Column(name = "contenu", columnDefinition = "TEXT", length = Integer.MAX_VALUE)
    private String contenu; // JSON stocké comme String
    
    @Column(name = "dependance_id", columnDefinition = "UUID")
    private UUID dependanceId;
    
    public TemplateTacheEntity() {}
    
    public TemplateTacheEntity(UUID id, String code, String libelle, String description,
                               TacheType type, int delaiJour, String contenu, UUID dependanceId) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.type = type;
        this.delaiJour = delaiJour;
        this.contenu = contenu;
        this.dependanceId = dependanceId;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TacheType getType() {
        return type;
    }
    
    public void setType(TacheType type) {
        this.type = type;
    }
    
    public int getDelaiJour() {
        return delaiJour;
    }
    
    public void setDelaiJour(int delaiJour) {
        this.delaiJour = delaiJour;
    }
    
    public String getContenu() {
        return contenu;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public UUID getDependanceId() {
        return dependanceId;
    }
    
    public void setDependanceId(UUID dependanceId) {
        this.dependanceId = dependanceId;
    }
}