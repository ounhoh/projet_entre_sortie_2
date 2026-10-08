package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import com.cese.process_entree_sortie.domain.utils.error.AgentAssigneInvalideException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public record InstanceGroupeTache (UUID id, LocalDate dateEcheance, String code,String libelle,
                                   StatutTache statut, UUID templateId, UUID processusId,
                                   List<InstanceTache> tacheList, List<UUID> agentAssigneIdList)
    implements NoeudTache{
    public InstanceGroupeTache {
        Objects.requireNonNull(id);
        Objects.requireNonNull(dateEcheance);
        Objects.requireNonNull(code);
        Objects.requireNonNull(templateId);
        Objects.requireNonNull(statut);
        Objects.requireNonNull(processusId);
        Objects.requireNonNull(agentAssigneIdList);
        
        // Vérifier qu'il y a au moins un agent assigné
        if (agentAssigneIdList.isEmpty()) {
            throw new AgentAssigneInvalideException("aucun agent n'a été assigné au groupe de tâches");
        }
        // Créer une copie immuable de la liste
        agentAssigneIdList = List.copyOf(agentAssigneIdList);
    }

    public InstanceGroupeTache ajouterTache(InstanceTache tache) // ajouter une tache au gropue de tache
    {
        if (tacheList.contains(tache))
        {
            throw  new IllegalArgumentException("tache est déjà dans ");
        }

        List<InstanceTache> tacheArrayList = new ArrayList<>(tacheList);
        tacheArrayList.add(tache);
        return new InstanceGroupeTache(id,dateEcheance,code,libelle,statut,templateId,
                processusId, List.copyOf(tacheArrayList), agentAssigneIdList);

    }

    public InstanceGroupeTache changerStatut() // changer le statut du groupe de tache
    {
        // Si toutes les tâches sont "à faire", le groupe est "enAttente"
        boolean toutesTachesAFaire = tacheList.stream()
            .allMatch(t -> t.statut().equals(StatutTache.aFaire));
        if (toutesTachesAFaire) {
            return new InstanceGroupeTache(id, dateEcheance, code, libelle, StatutTache.enAttente, templateId, processusId, tacheList, agentAssigneIdList);
        }
        
        // Si toutes les tâches sont faites, le groupe est fait
        if (tacheFaitAuComplet()) {
            return new InstanceGroupeTache(id, dateEcheance, code, libelle, StatutTache.fait, templateId, processusId, tacheList, agentAssigneIdList);
        }
        
        // Si le groupe est "fait" mais qu'au moins une tâche n'est plus "fait", passer en cours
        if (statut.equals(StatutTache.fait)) {
            boolean auMoinsUneTacheNonFaite = tacheList.stream()
                .anyMatch(t -> !t.statut().equals(StatutTache.fait));
            if (auMoinsUneTacheNonFaite) {
                return new InstanceGroupeTache(id, dateEcheance, code, libelle, StatutTache.enCours, templateId, processusId, tacheList, agentAssigneIdList);
            }
        }
        
        // Si le groupe est en attente et qu'au moins une tâche n'est plus "à faire", passer en cours
        if (statut.equals(StatutTache.enAttente)) {
            return new InstanceGroupeTache(id, dateEcheance, code, libelle, StatutTache.enCours, templateId, processusId, tacheList, agentAssigneIdList);
        }
        
        // Si le groupe est "en cours" et qu'aucune tâche n'est "en cours" ou "à faire", 
        // mais qu'au moins une n'est pas "fait", garder "en cours"
        // Sinon, si toutes sont "fait", passer à "fait" (déjà géré plus haut)
        
        return this;
    }


    public Boolean estDejaEnCours() // regarde si le groupe de tache a déjà commencé
    {
        return !statut.equals(StatutTache.enAttente);
    }


    public InstanceGroupeTache mettreDateEchanceAJour() // mettre à jour la date echeance(= max(tache.dateechance))
    {
        LocalDate newDateEcheance = tacheList.stream().map(InstanceTache::dateEcheance).max(LocalDate::compareTo).
                orElseThrow();
        return new InstanceGroupeTache(id,newDateEcheance,code,libelle,StatutTache.fait,templateId,processusId,tacheList,agentAssigneIdList);
    }
    
    public InstanceGroupeTache assignerAgent(UUID agentId) // assigner un nouvel agent pour recevoir les tâches du groupe
    {
        if (agentAssigneIdList().contains(agentId))
        {
            throw new AgentAssigneInvalideException("Agent étant déjà dans la liste des agents qui sont assignés à ce groupe de tâches");
        }
        List<UUID> newListeAgentAssigne = new ArrayList<>(List.copyOf(agentAssigneIdList));
        newListeAgentAssigne.add(agentId);
        return new InstanceGroupeTache(id,dateEcheance,code,libelle,statut,templateId,processusId,tacheList,List.copyOf(newListeAgentAssigne));
    }

    public InstanceGroupeTache retirerAgent(UUID agentId) // retirer un agent dans la liste des agents qui reçoivent les tâches du groupe
    {
        if (!agentAssigneIdList().contains(agentId))
        {
            throw new AgentAssigneInvalideException("Agent n'étant pas dans la liste des agents qui sont assignés à ce groupe de tâches");
        }
        List<UUID> newListeAgentAssigne = agentAssigneIdList.stream()
                .filter(i -> !i.equals(agentId))
                .collect(Collectors.toList());
        return new InstanceGroupeTache(id,dateEcheance,code,libelle,statut,templateId,processusId,tacheList,newListeAgentAssigne);
    }
    public Boolean estEnRetard() //  regarde si le groupe de tache est en retards
    {
        return tacheList.stream().anyMatch(i -> i.estEnRetard());
    }

    public Boolean tacheFaitAuComplet() // vérifie si toute les taches ont bien été faite
    {
        return tacheList.stream().allMatch(i ->i.statut().equals(StatutTache.fait));
    }

    private Optional<InstanceTache> trouverTahe(UUID tacheId, List<InstanceTache> taches)
    {
       return taches.stream().filter(tache -> tache.id().equals(tacheId)).findAny();
    }

    @Override
    public TacheType type() {
        return TacheType.groupeTache;
    }

    @Override
    public Optional<InstanceTache> getTache() {
        return Optional.empty();
    }

    @Override
    public Optional<InstanceGroupeTache> getGroupeTache() {
        return Optional.of(this);
    }
}
