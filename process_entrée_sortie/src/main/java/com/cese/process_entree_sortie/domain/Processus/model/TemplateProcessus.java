package com.cese.process_entree_sortie.domain.Processus.model;

import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

public record TemplateProcessus(UUID id, String codeProcessus, String libProcessus, String description,
                                TypeProcessus type, Boolean actif,
                                List<TemplateGroupeTache> groupeTacheList,
                                List<StatutProcessus> statusList,
                                List<TemplateDependance> dependanceList) {

    public TemplateProcessus {
        Objects.requireNonNull(id);
        Objects.requireNonNull(codeProcessus);
        Objects.requireNonNull(libProcessus);
        Objects.requireNonNull(statusList);
        Objects.requireNonNull(type);


        if (description != null && description.length() > 1000)
        {
            throw new IllegalArgumentException("description trop longue");

        }
    }


    private TemplateProcessus changerEtatActif(Boolean newEtat)
    {
        return new TemplateProcessus(id,codeProcessus,libProcessus,description,type,newEtat,
                groupeTacheList,statusList,dependanceList);
    }

    public TemplateProcessus rendreProcessActif()
    {
        return changerEtatActif(Boolean.TRUE);
    }

    public TemplateProcessus rendreProcessInactif()
    {
        return changerEtatActif(Boolean.FALSE);
    }
    public static Builder Builder()
    {
        return new Builder();
    }

    public static Builder builder(TemplateProcessus templateProcessus)
    {
        return new Builder(templateProcessus);
    }

    // builder dans le but de rajouter un éditeur de process à l'avenir
    public static class Builder
    {
        UUID id = null;
        String codeProcessus = "";
        String libProcessus = "";
        String description = "";
        TypeProcessus type = TypeProcessus.entree;
        Boolean actif = Boolean.FALSE;
        List<TemplateDependance> dependanceList = new ArrayList<>();
        List<TemplateGroupeTache> groupeTacheList = new ArrayList<>();
        List<StatutProcessus> statusList = new ArrayList<>();
        public Builder()
        {

        }

        public  Builder(TemplateProcessus templateProcessus)
        {
            id = templateProcessus.id;
            codeProcessus = templateProcessus.codeProcessus;
            libProcessus  = templateProcessus.libProcessus;
            description = templateProcessus.description;
            type = templateProcessus.type;
            actif = templateProcessus.actif;
            groupeTacheList = templateProcessus.groupeTacheList;
            statusList = templateProcessus.statusList;
            dependanceList = templateProcessus.dependanceList;
        }

        public Builder withId(UUID newId)
        {
            id = newId;
            return this;
        }

        public Builder withCodeProcess(String newCodeProcessus)
        {
           codeProcessus = newCodeProcessus;
           return this;
        }

        public Builder withLibProcess(String newLibProcess)
        {
            libProcessus = newLibProcess;
            return this;
        }

        public Builder withDescription(String newDescription)
        {
            description = newDescription;
            return this;
        }

        public Builder withTypeProcess(TypeProcessus newTypeProcess)
        {
            type = newTypeProcess;
            return this;
        }

        public Builder withEtatActif(Boolean newEtat)
        {
            actif = newEtat;
            return this;
        }
        public Builder withGroupeTacheList(List<TemplateGroupeTache> newTacheList)
        {
            Objects.requireNonNull(newTacheList);
            groupeTacheList = newTacheList;
            return this;
        }
        public Builder withDependanceList(List<TemplateDependance> newDependanceList)
        {
            Objects.requireNonNull(newDependanceList);
            dependanceList = newDependanceList;
            return this;
        }

        public Builder addDependance(TemplateDependance newDependance)
        {
           Objects.requireNonNull(newDependance);
           dependanceList.add(newDependance);
           return this;
        }

        public Builder addGroupeTache(TemplateGroupeTache newGroupe)
        {
            return addGroupeTache(newGroupe, -1);
        }


        public Builder addGroupeTache(TemplateGroupeTache newGroupe,Integer ordre)
        {
            Objects.requireNonNull(ordre);
            Objects.requireNonNull(newGroupe);
            if (ordre == -1)
            {
                groupeTacheList.add(newGroupe);
            }
            else
            {
                groupeTacheList.add(ordre,newGroupe);
            }
            return this;
        }

        public Builder removeGroupeTache(TemplateGroupeTache Groupe)
        {
            Objects.requireNonNull(Groupe);
            if(!groupeTacheList.remove(Groupe))
            {
                throw new IllegalArgumentException("groupe de tache n'est pas dans la liste des groupe de tache");
            }
            return this;
        }
        public Builder withStatutProcess(List<StatutProcessus> statutProcess)
        {
            Objects.requireNonNull(statutProcess);
            statusList = statutProcess;
            return this;
        }


        public Builder addStatutProcess(StatutProcessus newStatutProcessus)
        {
            return addStatutProcess(newStatutProcessus,-1);
        }

        public Builder addStatutProcess(StatutProcessus statutProcessus, Integer ordre)
        {
            Objects.requireNonNull(ordre);
            Objects.requireNonNull(statutProcessus);

            if (ordre == -1)
            {
                statusList.add(statutProcessus);
            }
            else {
                statusList.add(ordre,statutProcessus);
            }
            return  this;
        }

        public Builder removeStatutProcess(StatutProcessus statutProcessus)
        {
           Objects.requireNonNull(statutProcessus);
           if(!statusList.remove(statutProcessus))
           {
               throw new IllegalArgumentException("statut n'était pas dans la liste des status");
           }
           return this;
        }

        public TemplateProcessus build()
        {
            return new TemplateProcessus(id,codeProcessus,libProcessus,description,type,
                    actif,groupeTacheList,statusList,dependanceList);
        }
    }
}

