package com.cese.process_entree_sortie.domain.Tache.service;

import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.NoeudTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.util.ArrayList;
import java.util.List;

public class TacheService {


    /*
    * Vérifier qu'on peut bien lancer un groupe de tache
    * @param  le groupe de tache qu'on voudrait lancer
    * @return si oui ou non on peut la lancer
    */
   public Boolean TachePeutDemarrer(InstanceTache tache, List<InstanceDependance> dependances,
                                    List<NoeudTache> elementTacheList)
   {
       return ElementPeutDemarrer(tache,dependances,elementTacheList);
   }

    public Boolean GroupeTachePeutDemarrer(InstanceGroupeTache groupeTache, List<InstanceDependance> dependances,
                                     List<NoeudTache> elementTacheList)
    {
        return ElementPeutDemarrer(groupeTache,dependances,elementTacheList);
    }

    private Boolean ElementPeutDemarrer(NoeudTache elementTache, List<InstanceDependance> dependances,
                                       List<NoeudTache> elementTacheList)
    {
        if (TacheService.estDejatEnCours(elementTache))
        {
            return true;
        }
        List<InstanceDependance> rechercheDependance = new ArrayList<>();
        switch (elementTache.type())
        {
            case tache -> rechercheDependance = checkDependanceTache(elementTache,dependances);
            case groupeTache -> rechercheDependance = checkDependanceGroupeTache(elementTache,dependances);
            case tache_et_groupeTache -> rechercheDependance = checkDependanceTache(elementTache,dependances); // Traiter comme une tâche
        }

        if (rechercheDependance.isEmpty())
        {
            return true;
        }
       return rechercheDependance.stream().allMatch(dep ->
        dependanceTerminer(dep, elementTacheList)
       );
    }


    /**
     * Dans la nouvelle structure, les dépendances sont uniquement entre tâches.
     * Un groupe n'a pas de dépendances directes, mais ses tâches peuvent en avoir.
     * Cette méthode retourne une liste vide car les groupes n'ont plus de dépendances.
     */
    private static List<InstanceDependance> checkDependanceGroupeTache(NoeudTache element, List<InstanceDependance> dependances)
    {
        // Dans la nouvelle structure, les dépendances sont uniquement entre tâches
        // Un groupe n'a pas de dépendances directes
        return List.of();
    }

    /**
     * Trouve les dépendances où cette tâche est une cible.
     * Dans la nouvelle structure, une dépendance a une source (tâche) et plusieurs cibles (tâches).
     */
    private static List<InstanceDependance> checkDependanceTache(NoeudTache element, List<InstanceDependance> dependances)
    {
        return dependances.stream()
                .filter(dep -> dep.cibleTacheIds().contains(element.id()))
                .toList();
    }

    // vérifie si un groupe de tache ou une tache est en cours
    // Pour les groupes : enAttente -> enCours -> fait
    // Pour les tâches : aFaire -> enCours -> fait (pas de enAttente)
    private  static Boolean estDejatEnCours(NoeudTache elementTache)
    {
       // Pour les groupes, vérifier si ce n'est pas enAttente
       // Pour les tâches, vérifier si ce n'est pas aFaire (car les tâches commencent à aFaire)
       if (elementTache.type().equals(TacheType.tache)) {
           // Pour les tâches, elles sont "en cours" si elles sont à enCours ou fait
           return elementTache.statut().equals(StatutTache.enCours) || 
                  elementTache.statut().equals(StatutTache.fait);
       } else {
           // Pour les groupes, vérifier si ce n'est pas enAttente
           return !elementTache.statut().equals(StatutTache.enAttente);
       }
    }

    /**
     * Vérifie que la dépendance est terminée (la tâche source est terminée).
     * @param dependance La dépendance à vérifier
     * @param elementTacheList La liste des nœuds de tâche
     * @return true si la tâche source est terminée, false sinon
     */
    public Boolean dependanceTerminer(InstanceDependance dependance,
                                           List<NoeudTache> elementTacheList)
    {
        NoeudTache elementTache = elementTacheList.stream()
                .filter(element -> element.id().equals(dependance.sourceTacheId()))
                .findFirst()
                .orElse(null);
        
        if (elementTache == null) {
            return false; // Source non trouvée = dépendance non terminée
        }
        
        return elementTache.statut().equals(StatutTache.fait);
    }

    /*
    public InstanceGroupeTache demarrerGroupeTache() // changer de statut le groupe de tache pour le statut en cours
    {
        if (!statut.equals(StatutTache.enAttente))
        {
            throw new GroupeTacheInvalideException("Groupe tache pas avec le bon statut");
        }
        if (tacheList.size() < 1)
        {
            throw new GroupeTacheInvalideException("Groupe de tache sans aucune tache");
        }

        // Passage de toutes les tâches à l'état démarrer
        List<InstanceTache> newTacheList = tacheList.stream().map(InstanceTache::demarer).toList();
        return new InstanceGroupeTache(id,dateEchance,codeGroupeTache,StatutTache.enCours,templateId,
                processusId,newTacheList);
    }

    public InstanceGroupeTache demarrerGroupeTache(InstanceGroupeTache groupe,List<InstanceDependance> dependances) // lance le groupe de tache
    {

       return groupe.changerStatut();
    }

     */
    //public InstanceGroupeTache lancerGroupeTache(InstanceGroupeTache groupe, L)


    /*
    // renvoie toute les tâches pouvant commencer
    public List<InstanceTache> getTachePouvantCommencer(InstanceGroupeTache groupeTache,
                                                        List<InstanceGroupeTache> groupeTaches,
                                                        List<InstanceTache> taches,
                                                        List<InstanceDependance> Dependances)
    {
        if(!groupeTache.estDejaEnCours())
        {
            return List.of();
        }
        else
        {
            return groupeTache.tacheList().stream().f
        }
    }

     */
}