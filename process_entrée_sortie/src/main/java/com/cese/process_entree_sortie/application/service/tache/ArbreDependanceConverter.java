package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.*;
import com.cese.process_entree_sortie.domain.Tache.model.*;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de conversion pour construire l'arbre des dépendances
 */
@Component
public class ArbreDependanceConverter {
    
    public ArbreDependanceConverter() {
    }
    
    /**
     * Construit l'arbre des dépendances à partir des groupes, tâches et dépendances
     */
    public ProcessusArbreDTO construireArbre(
            List<InstanceGroupeTache> groupes,
            List<InstanceTache> taches,
            List<InstanceDependance> dependances,
            ProcessusDTO processInfo) {
        
        // Créer un map pour accès rapide par ID
        Map<UUID, NoeudTacheDTO> noeudsMap = new HashMap<>();
        
        // Convertir tous les groupes en nœuds (sans enfants pour l'instant)
        groupes.forEach(groupe -> {
            GroupeTacheNoeudDTO noeud = convertirGroupeEnNoeud(groupe, taches);
            noeudsMap.put(groupe.id(), noeud);
        });
        
        // Convertir toutes les tâches en nœuds (sans enfants pour l'instant)
        taches.forEach(tache -> {
            TacheNoeudDTO noeud = convertirTacheEnNoeud(tache);
            noeudsMap.put(tache.id(), noeud);
        });
        
        // Construire les relations parent-enfant basées sur les dépendances
        // Map pour stocker les enfants de chaque nœud
        Map<UUID, List<NoeudTacheDTO>> enfantsMap = new HashMap<>();
        Map<UUID, Integer> ordreMap = new HashMap<>(); // Pour stocker l'ordre des dépendances
        
        int ordreIndex = 0;
        for (InstanceDependance dep : dependances) {
            NoeudTacheDTO source = noeudsMap.get(dep.sourceTacheId());
            
            // Dans la nouvelle structure, une dépendance a plusieurs cibles (tâches uniquement)
            for (UUID cibleTacheId : dep.cibleTacheIds()) {
                NoeudTacheDTO cible = noeudsMap.get(cibleTacheId);
                
                if (source != null && cible != null) {
                    enfantsMap.computeIfAbsent(source.id(), k -> new ArrayList<>()).add(cible);
                    // L'ordre est basé sur l'ordre d'apparition dans les dépendances
                    ordreMap.put(cible.id(), ordreIndex++);
                }
            }
        }
        
        // Reconstruire les nœuds avec leurs enfants et ordre (récursivement)
        Map<UUID, NoeudTacheDTO> noeudsAvecEnfants = new HashMap<>();
        
        // Reconstruire tous les nœuds
        for (UUID id : noeudsMap.keySet()) {
            reconstruireNoeudRecursif(id, noeudsMap, enfantsMap, ordreMap, noeudsAvecEnfants);
        }
        
        // Trouver les racines (nœuds sans dépendances entrantes)
        // Dans la nouvelle structure, les dépendances sont uniquement entre tâches
        Set<UUID> noeudsAvecDependancesEntrantes = dependances.stream()
            .flatMap(dep -> dep.cibleTacheIds().stream()) // Toutes les tâches cibles
            .collect(Collectors.toSet());
        
        List<NoeudTacheDTO> racines = noeudsAvecEnfants.values().stream()
            .filter(noeud -> !noeudsAvecDependancesEntrantes.contains(noeud.id()))
            .sorted(Comparator.comparing(n -> n.ordre() != null ? n.ordre() : Integer.MAX_VALUE))
            .collect(Collectors.toList());
        
        return new ProcessusArbreDTO(processInfo, racines);
    }
    
    private GroupeTacheNoeudDTO convertirGroupeEnNoeud(
            InstanceGroupeTache groupe, 
            List<InstanceTache> toutesTaches) {
        
        // Tâches directement dans le groupe (sans dépendances)
        List<TacheNoeudDTO> tachesInternes = toutesTaches.stream()
            .filter(t -> t.groupeTacheId() != null && t.groupeTacheId().equals(groupe.id()))
            .map(this::convertirTacheEnNoeud)
            .collect(Collectors.toList());
        
        return new GroupeTacheNoeudDTO(
            groupe.id(),
            groupe.code(),
            groupe.libelle(),
            groupe.statut(),
            groupe.dateEcheance(),
            null, // ordre sera défini par les dépendances
            tachesInternes,
            Optional.empty(),
            Optional.empty(),
            new ArrayList<>() // enfants seront remplis lors de la construction de l'arbre
        );
    }
    
    /**
     * Reconstruit récursivement un nœud avec ses enfants
     */
    private NoeudTacheDTO reconstruireNoeudRecursif(
            UUID id,
            Map<UUID, NoeudTacheDTO> noeudsMap,
            Map<UUID, List<NoeudTacheDTO>> enfantsMap,
            Map<UUID, Integer> ordreMap,
            Map<UUID, NoeudTacheDTO> noeudsAvecEnfants) {
        
        // Vérifier si déjà construit (éviter les cycles et les doublons)
        if (noeudsAvecEnfants.containsKey(id)) {
            return noeudsAvecEnfants.get(id);
        }
        
        NoeudTacheDTO noeud = noeudsMap.get(id);
        if (noeud == null) {
            return null;
        }
        
        List<NoeudTacheDTO> enfantsIds = enfantsMap.getOrDefault(id, new ArrayList<>());
        // Reconstruire récursivement chaque enfant
        List<NoeudTacheDTO> enfants = enfantsIds.stream()
            .map(n -> reconstruireNoeudRecursif(n.id(), noeudsMap, enfantsMap, ordreMap, noeudsAvecEnfants))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Trier les enfants par ordre
        enfants.sort(Comparator.comparing(n -> ordreMap.getOrDefault(n.id(), Integer.MAX_VALUE)));
        
        // Déterminer fils gauche (première tâche) et fils droit (premier groupe)
        Optional<NoeudTacheDTO> filsGauche = enfants.stream()
            .filter(n -> n.type() == TacheType.tache)
            .findFirst();
        
        Optional<NoeudTacheDTO> filsDroit = enfants.stream()
            .filter(n -> n.type() == TacheType.groupeTache)
            .findFirst();
        
        Integer ordre = ordreMap.get(id);
        
        // Reconstruire le nœud avec les enfants
        NoeudTacheDTO noeudAvecEnfants;
        if (noeud instanceof TacheNoeudDTO tacheNoeud) {
            noeudAvecEnfants = new TacheNoeudDTO(
                tacheNoeud.id(),
                tacheNoeud.code(),
                tacheNoeud.libelle(),
                tacheNoeud.statut(),
                tacheNoeud.dateEcheance(),
                ordre,
                tacheNoeud.contenu(),
                filsGauche,
                filsDroit,
                enfants
            );
        } else if (noeud instanceof GroupeTacheNoeudDTO groupeNoeud) {
            noeudAvecEnfants = new GroupeTacheNoeudDTO(
                groupeNoeud.id(),
                groupeNoeud.code(),
                groupeNoeud.libelle(),
                groupeNoeud.statut(),
                groupeNoeud.dateEcheance(),
                ordre,
                groupeNoeud.tachesInternes(),
                filsGauche,
                filsDroit,
                enfants
            );
        } else {
            noeudAvecEnfants = noeud; // Ne devrait pas arriver
        }
        
        noeudsAvecEnfants.put(id, noeudAvecEnfants);
        return noeudAvecEnfants;
    }
    
    private TacheNoeudDTO convertirTacheEnNoeud(InstanceTache tache) {
        // Convertir TacheContenu en Map
        Map<String, Object> contenu = new HashMap<>();
        if (tache.contenu() != null && tache.contenu().contenuTache() != null) {
            contenu = tache.contenu().contenuTache();
        }
        
        return new TacheNoeudDTO(
            tache.id(),
            tache.code(),
            tache.libelle(),
            tache.statut(),
            tache.dateEcheance(),
            null, // ordre sera défini par les dépendances
            contenu,
            Optional.empty(),
            Optional.empty(),
            new ArrayList<>()
        );
    }
}
