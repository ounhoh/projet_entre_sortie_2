package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;

import java.util.Objects;
import java.util.UUID;

public record TemplateGroupeTache (UUID id, String codeTemplate , String codeDirection, String libGroupTache,
                                   UUID statutProcessusId, UUID directionId, UUID templateProcessusId, boolean isDirectionConcernee){
    public  TemplateGroupeTache {
        Objects.requireNonNull(id);
        Objects.requireNonNull(codeTemplate);
        Objects.requireNonNull(statutProcessusId);
        Objects.requireNonNull(libGroupTache);
        Objects.requireNonNull(templateProcessusId);
    }

    public  static Builder Builder()
    {
        return  new Builder();
    }

    public static class Builder{ // Builder d'un nouveau template de groupe de tache si un joueur on fait un editeur de process
       UUID id = null;
       String codeTemplate = "";
       String codeDirection = "";
       String libGroupTache = "";
       UUID statutProcessusId = null;
       UUID directionId = null;
       UUID templateProcessusId = null;
       boolean isDirectionConcernee = false;

       public Builder()
       {

       }

       public Builder(TemplateGroupeTache templateGroupeTache)
       {
          id = templateGroupeTache.id;
          codeTemplate = templateGroupeTache.codeTemplate;
          codeDirection = templateGroupeTache.codeDirection;
          libGroupTache = templateGroupeTache.libGroupTache;
          statutProcessusId = templateGroupeTache.statutProcessusId;
          directionId = templateGroupeTache.directionId;
          templateProcessusId = templateGroupeTache.templateProcessusId;
          isDirectionConcernee = templateGroupeTache.isDirectionConcernee;
       }

       public Builder withId(UUID newId)
       {
           id = newId;
           return this;
       }
       public Builder withCodeTemplate(String newCodeTemplate)
       {
           codeTemplate = newCodeTemplate;
           return this;
       }
       public Builder withCodeDirection(String newCodeDirection)
       {
           codeDirection  = newCodeDirection;
           return this;
       }
       public Builder withlibGroupTache(String newLibGroupeTache)
       {
           libGroupTache = newLibGroupeTache;
           return this;
       }
       public Builder withStatutProcessusId(UUID newStatutProcessusId)
       {
           statutProcessusId = newStatutProcessusId;
           return this;
       }
       public Builder withDirectionId(UUID newDirectionId)
       {
           directionId = newDirectionId;
           return this;
       }

       public Builder withTemplateProcessusId(UUID newTemplateProcessusId)
       {
           templateProcessusId = newTemplateProcessusId;
           return this;
       }
       
       public Builder withIsDirectionConcernee(boolean newIsDirectionConcernee)
       {
           isDirectionConcernee = newIsDirectionConcernee;
           return this;
       }
       
       public TemplateGroupeTache build()
       {
           return new TemplateGroupeTache(id,codeTemplate,codeDirection,libGroupTache,
               statutProcessusId,directionId,templateProcessusId,isDirectionConcernee);
       }
    }
}
