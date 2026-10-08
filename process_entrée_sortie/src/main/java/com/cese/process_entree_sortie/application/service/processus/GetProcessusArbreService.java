package com.cese.process_entree_sortie.application.service.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreFiltreDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.NoeudTacheDTO;
import com.cese.process_entree_sortie.application.port.in.processus.GetProcessusArbreApi;
import com.cese.process_entree_sortie.application.service.tache.ArbreDependanceAnalyzer;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.application.service.tache.ArbreDependanceConverter;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProcessusArbreService implements GetProcessusArbreApi {
    
    private final ProcessusSpi processusSpi;
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    private final DependanceSpi dependanceSpi;
    private final ProcessusConverter processusConverter;
    private final ArbreDependanceConverter arbreConverter;
    private final ArbreDependanceAnalyzer arbreAnalyzer;
    
    public GetProcessusArbreService(
            ProcessusSpi processusSpi,
            GroupeTacheSpi groupeTacheSpi,
            TacheSpi tacheSpi,
            DependanceSpi dependanceSpi,
            ProcessusConverter processusConverter,
            ArbreDependanceConverter arbreConverter,
            ArbreDependanceAnalyzer arbreAnalyzer) {
        this.processusSpi = processusSpi;
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
        this.dependanceSpi = dependanceSpi;
        this.processusConverter = processusConverter;
        this.arbreConverter = arbreConverter;
        this.arbreAnalyzer = arbreAnalyzer;
    }
    
    @Override
    public ProcessusArbreDTO getProcessusArbre(UUID processusId) {
        // Récupérer le processus
        InstanceProcessus processus = processusSpi.findById(processusId)
            .orElseThrow(() -> new RuntimeException("Processus non trouvé avec l'ID: " + processusId));
        
        // Récupérer les groupes de tâches
        var groupes = groupeTacheSpi.findByProcessusId(processusId);
        
        // Récupérer toutes les tâches du processus
        var taches = tacheSpi.findbyProcessusId(processusId);
        
        // Récupérer les dépendances
        var dependances = dependanceSpi.findByProcessusId(processusId);
        
        // Convertir le processus en DTO
        ProcessusDTO processusDTO = processusConverter.convertirEnDTO(processus);
        
        // Construire l'arbre
        return arbreConverter.construireArbre(groupes, taches, dependances, processusDTO);
    }
    
    /**
     * Récupère l'arbre avec des filtres et statistiques
     */
    public ProcessusArbreFiltreDTO getProcessusArbreFiltre(
            UUID processusId,
            ProcessusArbreFiltreDTO.StatutFiltre statutFiltre,
            UUID agentFiltre) {
        
        // Récupérer l'arbre de base
        ProcessusArbreDTO arbre = getProcessusArbre(processusId);
        
        // Récupérer les dépendances pour l'analyse
        var dependances = dependanceSpi.findByProcessusId(processusId);
        
        // Calculer les statistiques
        var stats = arbreAnalyzer.calculerStats(arbre.racines());
        
        // Trouver les nœuds prêts et bloqués
        var noeudsPrets = arbreAnalyzer.trouverNoeudsPretsADemarrer(arbre.racines(), dependances);
        var noeudsBloques = arbreAnalyzer.trouverNoeudsBloques(arbre.racines(), dependances);
        
        // Filtrer selon le statut demandé
        List<NoeudTacheDTO> racinesFiltrees = arbre.racines();
        if (statutFiltre == ProcessusArbreFiltreDTO.StatutFiltre.PRETS_A_DEMARRER) {
            racinesFiltrees = noeudsPrets;
        } else if (statutFiltre == ProcessusArbreFiltreDTO.StatutFiltre.BLOQUES) {
            racinesFiltrees = noeudsBloques;
        } else if (statutFiltre == ProcessusArbreFiltreDTO.StatutFiltre.COMPLETES) {
            racinesFiltrees = arbreAnalyzer.filtrerParStatut(arbre.racines(), StatutTache.fait);
        } else if (statutFiltre == ProcessusArbreFiltreDTO.StatutFiltre.EN_COURS) {
            racinesFiltrees = arbreAnalyzer.filtrerParStatut(arbre.racines(), StatutTache.enCours);
        } else if (statutFiltre == ProcessusArbreFiltreDTO.StatutFiltre.EN_ATTENTE) {
            racinesFiltrees = arbreAnalyzer.filtrerParStatut(arbre.racines(), StatutTache.enAttente);
        }
        
        return new ProcessusArbreFiltreDTO(
            arbre.processInfo(),
            racinesFiltrees,
            statutFiltre,
            agentFiltre,
            stats.totalNoeuds(),
            stats.noeudsCompletes(),
            stats.noeudsEnCours(),
            stats.noeudsEnAttente(),
            stats.pourcentageAvancement(),
            noeudsPrets,
            noeudsBloques
        );
    }
}
