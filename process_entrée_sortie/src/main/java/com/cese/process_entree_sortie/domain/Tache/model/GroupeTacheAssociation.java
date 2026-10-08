package com.cese.process_entree_sortie.domain.Tache.model;


import java.util.Objects;

public record GroupeTacheAssociation (TemplateGroupeTache templateGroupeTache, TemplateTache templateTache){
    public GroupeTacheAssociation
    {
        Objects.requireNonNull(templateTache);
        Objects.requireNonNull(templateGroupeTache);
    }
}
