package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.NoeudTacheDTO;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour analyser et filtrer l'arbre des dépendances
 */
@Component
public class ArbreDependanceAnalyzer {
    
    /**
     * Calcule les statistiques de l'arbre
     */
    public ArbreStats calculerStats(List<NoeudTacheDTO> racines) {
        int total = 0;
        int completes = 0;
        int enCours = 0;
        int enAttente = 0;
        
        for (NoeudTacheDTO racine : racines) {
            StatsResultat resultat = compterNoeuds(racine);
            total += resultat.total;
            completes += resultat.completes;
            enCours += resultat.enCours;
            enAttente += resultat.enAttente;
        }
        
        double pourcentage = total > 0 ? (completes * 100.0 / total) : 0.0;
        
        return new ArbreStats(total, completes, enCours, enAttente, pourcentage);
    }
    
    /**
     * Trouve les nœuds prêts à démarrer (sans dépendances non complétées)
     */
    public List<NoeudTacheDTO> trouverNoeudsPretsADemarrer(
            List<NoeudTacheDTO> racines,
            List<InstanceDependance> dependances) {
        
        Set<UUID> noeudsCompletes = new HashSet<>();
        Set<UUID> noeudsPrets = new HashSet<>();
        
        // Trouver tous les nœuds complétés
        for (NoeudTacheDTO racine : racines) {
            trouverNoeudsCompletes(racine, noeudsCompletes);
        }
        
        // Trouver les nœuds prêts (dont toutes les dépendances sont complétées)
        for (NoeudTacheDTO racine : racines) {
            trouverNoeudsPretsRecursif(racine, dependances, noeudsCompletes, noeudsPrets, new HashSet<>());
        }
        
        return noeudsPrets.stream()
            .map(id -> trouverNoeudParId(racines, id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Trouve les nœuds bloqués (avec dépendances non complétées)
     */
    public List<NoeudTacheDTO> trouverNoeudsBloques(
            List<NoeudTacheDTO> racines,
            List<InstanceDependance> dependances) {
        
        Set<UUID> noeudsCompletes = new HashSet<>();
        Set<UUID> noeudsPrets = new HashSet<>();
        Set<UUID> noeudsBloques = new HashSet<>();
        
        // Trouver tous les nœuds complétés
        for (NoeudTacheDTO racine : racines) {
            trouverNoeudsCompletes(racine, noeudsCompletes);
        }
        
        // Trouver les nœuds prêts
        for (NoeudTacheDTO racine : racines) {
            trouverNoeudsPretsRecursif(racine, dependances, noeudsCompletes, noeudsPrets, new HashSet<>());
        }
        
        // Les nœuds bloqués sont ceux qui ne sont ni complétés ni prêts
        for (NoeudTacheDTO racine : racines) {
            trouverNoeudsBloquesRecursif(racine, noeudsCompletes, noeudsPrets, noeudsBloques);
        }
        
        return noeudsBloques.stream()
            .map(id -> trouverNoeudParId(racines, id))
            .filter(Objects::nonNull)
            .filter(n -> n.statut() != StatutTache.fait) // Exclure les déjà complétés
            .collect(Collectors.toList());
    }
    
    /**
     * Filtre l'arbre par statut
     */
    public List<NoeudTacheDTO> filtrerParStatut(List<NoeudTacheDTO> racines, StatutTache statut) {
        return racines.stream()
            .map(racine -> filtrerNoeudParStatut(racine, statut))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    // Méthodes privées utilitaires
    
    private StatsResultat compterNoeuds(NoeudTacheDTO noeud) {
        int total = 1;
        int completes = noeud.statut() == StatutTache.fait ? 1 : 0;
        int enCours = noeud.statut() == StatutTache.enCours ? 1 : 0;
        int enAttente = noeud.statut() == StatutTache.enAttente ? 1 : 0;
        
        for (NoeudTacheDTO enfant : noeud.enfants()) {
            StatsResultat enfantStats = compterNoeuds(enfant);
            total += enfantStats.total;
            completes += enfantStats.completes;
            enCours += enfantStats.enCours;
            enAttente += enfantStats.enAttente;
        }
        
        return new StatsResultat(total, completes, enCours, enAttente);
    }
    
    private void trouverNoeudsCompletes(NoeudTacheDTO noeud, Set<UUID> completes) {
        if (noeud.statut() == StatutTache.fait) {
            completes.add(noeud.id());
        }
        for (NoeudTacheDTO enfant : noeud.enfants()) {
            trouverNoeudsCompletes(enfant, completes);
        }
    }
    
    private void trouverNoeudsPretsRecursif(
            NoeudTacheDTO noeud,
            List<InstanceDependance> dependances,
            Set<UUID> noeudsCompletes,
            Set<UUID> noeudsPrets,
            Set<UUID> visite) {
        
        if (visite.contains(noeud.id())) {
            return; // Éviter les cycles
        }
        visite.add(noeud.id());
        
        // Vérifier si toutes les dépendances sont complétées
        // Dans la nouvelle structure, une dépendance a une source (tâche) et plusieurs cibles (tâches)
        // On cherche les dépendances où ce nœud est une cible
        boolean toutesDependancesCompletes = dependances.stream()
            .filter(dep -> dep.cibleTacheIds().contains(noeud.id()))
            .allMatch(dep -> noeudsCompletes.contains(dep.sourceTacheId()));
        
        if (toutesDependancesCompletes && noeud.statut() != StatutTache.fait) {
            noeudsPrets.add(noeud.id());
        }
        
        for (NoeudTacheDTO enfant : noeud.enfants()) {
            trouverNoeudsPretsRecursif(enfant, dependances, noeudsCompletes, noeudsPrets, visite);
        }
    }
    
    private void trouverNoeudsBloquesRecursif(
            NoeudTacheDTO noeud,
            Set<UUID> noeudsCompletes,
            Set<UUID> noeudsPrets,
            Set<UUID> noeudsBloques) {
        
        if (noeud.statut() != StatutTache.fait && !noeudsPrets.contains(noeud.id())) {
            noeudsBloques.add(noeud.id());
        }
        
        for (NoeudTacheDTO enfant : noeud.enfants()) {
            trouverNoeudsBloquesRecursif(enfant, noeudsCompletes, noeudsPrets, noeudsBloques);
        }
    }
    
    private NoeudTacheDTO filtrerNoeudParStatut(NoeudTacheDTO noeud, StatutTache statut) {
        List<NoeudTacheDTO> enfantsFiltres = noeud.enfants().stream()
            .map(enfant -> filtrerNoeudParStatut(enfant, statut))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (noeud.statut() == statut || !enfantsFiltres.isEmpty()) {
            // Reconstruire le nœud avec les enfants filtrés
            // Pour simplifier, on retourne le nœud tel quel si lui-même ou ses enfants matchent
            return noeud;
        }
        
        return null;
    }
    
    private NoeudTacheDTO trouverNoeudParId(List<NoeudTacheDTO> racines, UUID id) {
        for (NoeudTacheDTO racine : racines) {
            NoeudTacheDTO trouve = trouverNoeudRecursif(racine, id);
            if (trouve != null) {
                return trouve;
            }
        }
        return null;
    }
    
    private NoeudTacheDTO trouverNoeudRecursif(NoeudTacheDTO noeud, UUID id) {
        if (noeud.id().equals(id)) {
            return noeud;
        }
        for (NoeudTacheDTO enfant : noeud.enfants()) {
            NoeudTacheDTO trouve = trouverNoeudRecursif(enfant, id);
            if (trouve != null) {
                return trouve;
            }
        }
        return null;
    }
    
    // Classes internes pour les résultats
    private record StatsResultat(int total, int completes, int enCours, int enAttente) {}
    
    public record ArbreStats(
        int totalNoeuds,
        int noeudsCompletes,
        int noeudsEnCours,
        int noeudsEnAttente,
        double pourcentageAvancement
    ) {}
}
