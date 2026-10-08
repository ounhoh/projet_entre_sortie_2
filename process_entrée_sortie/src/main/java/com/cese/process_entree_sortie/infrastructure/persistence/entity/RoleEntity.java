package com.cese.process_entree_sortie.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "role")
public class RoleEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "libelle", nullable = false)
    private String libelle;
    
    @Column(name = "valeur", nullable = false, unique = true)
    private Integer valeur;
    
    public RoleEntity() {}
    
    public RoleEntity(UUID id, String code, String libelle, Integer valeur) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.valeur = valeur;
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
    
    public Integer getValeur() {
        return valeur;
    }
    
    public void setValeur(Integer valeur) {
        this.valeur = valeur;
    }
}
