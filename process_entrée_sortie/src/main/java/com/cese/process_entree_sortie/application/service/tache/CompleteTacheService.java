package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.rule.RuleExecutionContext;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.CompleteTacheApi;
import com.cese.process_entree_sortie.application.port.out.dependance.DependanceSpi;
import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.application.service.notification.NotificationService;
import com.cese.process_entree_sortie.application.service.rule.RuleEngine;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CompleteTacheService implements CompleteTacheApi {
    
    private static final Logger logger = LoggerFactory.getLogger(CompleteTacheService.class);
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    private final NotificationService notificationService;
    private final RuleEngine ruleEngine;
    private final GroupeTacheSpi groupeTacheSpi;
    private final ProcessusSpi processusSpi;
    private final DependanceSpi dependanceSpi;
    
    public CompleteTacheService(
            TacheSpi tacheSpi, 
            TacheConverter converter, 
            NotificationService notificationService,
            RuleEngine ruleEngine,
            GroupeTacheSpi groupeTacheSpi,
            ProcessusSpi processusSpi,
            DependanceSpi dependanceSpi) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
        this.notificationService = notificationService;
        this.ruleEngine = ruleEngine;
        this.groupeTacheSpi = groupeTacheSpi;
        this.processusSpi = processusSpi;
        this.dependanceSpi = dependanceSpi;
    }
    
    @Override
    public TacheDTO completeTache(UUID tacheId) {
        InstanceTache tache = tacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + tacheId));
        
        // Si la tâche est à "à faire", la démarrer automatiquement avant de la terminer
        InstanceTache tacheAPreparer = tache;
        if (tache.statut().equals(StatutTache.aFaire)) {
            logger.info("La tâche {} est à 'à faire', démarrage automatique avant complétion", tacheId);
            tacheAPreparer = tache.demarer(); // aFaire → enCours
            tacheSpi.save(tacheAPreparer); // Sauvegarder l'état "en cours"
        }
        
        // Maintenant on peut terminer (la tâche est soit déjà enCours, soit vient d'être mise enCours)
        InstanceTache tacheTerminee = tacheAPreparer.terminer(); // enCours → fait
        InstanceTache tacheSauvegarde = tacheSpi.save(tacheTerminee);
        
        // Exécuter les règles si la tâche a des actions définies dans son contenu
        // On vérifie la présence de la clé "actions" dans le contenu
        // IMPORTANT: Les règles doivent être exécutées avec succès pour que la tâche soit considérée comme complétée
        boolean hasActions = tacheSauvegarde.contenu() != null && 
                            tacheSauvegarde.contenu().contenuTache() != null &&
                            tacheSauvegarde.contenu().contenuTache().containsKey("actions");
        
        if (hasActions) {
            // Les règles doivent être exécutées avec succès - aucune exception ne sera capturée
            // Si une règle échoue, la transaction sera annulée et la tâche ne sera pas marquée comme complétée
            RuleExecutionContext context = buildContext(tacheSauvegarde);
            ruleEngine.executeRules(tacheSauvegarde, context);
            logger.info("Règles exécutées avec succès pour la tâche {}", tacheSauvegarde.id());
        }
        
        // Activer les tâches dépendantes de cette tâche
        activerTachesDependantes(tacheSauvegarde.id());
        
        // Vérifier si un groupe d'une autre direction devient disponible
        notificationService.verifierEtNotifierGroupeDisponible(tacheSauvegarde.id());
        
        return converter.convertirEnDTO(tacheSauvegarde);
    }
    
    /**
     * Active toutes les tâches qui dépendent de la tâche source complétée
     */
    private void activerTachesDependantes(UUID sourceTacheId) {
        try {
            // Trouver la dépendance dont cette tâche est la source
            Optional<InstanceDependance> dependanceOpt = dependanceSpi.findBySourceTacheId(sourceTacheId);
            
            if (dependanceOpt.isPresent()) {
                InstanceDependance dependance = dependanceOpt.get();
                
                // Activer toutes les tâches cibles
                for (UUID cibleTacheId : dependance.cibleTacheIds()) {
                    Optional<InstanceTache> cibleTacheOpt = tacheSpi.findById(cibleTacheId);
                    if (cibleTacheOpt.isPresent()) {
                        InstanceTache cibleTache = cibleTacheOpt.get();
                        
                        // Démarrer la tâche (aFaire -> enCours) si elle est à "à faire"
                        if (cibleTache.statut().equals(StatutTache.aFaire)) {
                            logger.info("Démarrage de la tâche {} après complétion de la tâche source {}", 
                                      cibleTacheId, sourceTacheId);
                            InstanceTache tacheEnCours = cibleTache.demarer(); // aFaire -> enCours
                            tacheSpi.save(tacheEnCours);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation des tâches dépendantes pour la tâche {}: {}", 
                        sourceTacheId, e.getMessage(), e);
            // Ne pas faire échouer la complétion de la tâche si l'activation échoue
        }
    }
    
    /**
     * Construit le contexte d'exécution des règles
     */
    private RuleExecutionContext buildContext(InstanceTache tache) {
        // Récupérer le groupe de la tâche
        InstanceGroupeTache groupe = groupeTacheSpi.findById(tache.groupeTacheId())
            .orElseThrow(() -> new RuntimeException("Groupe de tâche non trouvé"));
        
        // Récupérer le processus
        InstanceProcessus processus = processusSpi.findById(groupe.processusId())
            .orElseThrow(() -> new RuntimeException("Processus non trouvé"));
        
        // Extraire les données du formulaire depuis le contenu de la tâche
        Map<String, Object> formulaireData = extractFormulaireData(tache);
        
        return new RuleExecutionContext(formulaireData, processus, tache);
    }
    
    /**
     * Extrait les données du formulaire depuis le contenu de la tâche
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractFormulaireData(InstanceTache tache) {
        logger.info("[extractFormulaireData] Extraction des données pour la tâche {}", tache.id());
        
        if (tache.contenu() == null || tache.contenu().contenuTache() == null) {
            logger.warn("[extractFormulaireData] Contenu de la tâche est NULL");
            return Map.of();
        }
        
        Map<String, Object> contenu = tache.contenu().contenuTache();
        logger.info("[extractFormulaireData] Contenu de la tâche contient {} clés: {}", contenu.size(), contenu.keySet());
        
        // Le contenu peut être structuré de différentes manières
        // Format 1: Les valeurs sont directement dans le contenu
        // Format 2: Les valeurs sont dans une clé "formulaire" ou "data"
        
        if (contenu.containsKey("formulaire")) {
            Object formulaireObj = contenu.get("formulaire");
            if (formulaireObj instanceof Map) {
                logger.info("[extractFormulaireData] Données trouvées dans clé 'formulaire'");
                return (Map<String, Object>) formulaireObj;
            }
        }
        
        if (contenu.containsKey("data")) {
            Object dataObj = contenu.get("data");
            if (dataObj instanceof Map) {
                logger.info("[extractFormulaireData] Données trouvées dans clé 'data'");
                return (Map<String, Object>) dataObj;
            }
        }
        
        // Sinon, retourner le contenu tel quel (en excluant les métadonnées comme "fields", "actions")
        // IMPORTANT: Extraire aussi les données de la clé "reponses" pour que les règles y aient accès
        Map<String, Object> formulaireData = new java.util.HashMap<>();
        
        // D'abord, extraire les réponses si elles existent dans une clé séparée "reponses"
        // Cela permet aux règles d'accéder aux données comme directionId, fonction, etc.
        logger.info("[extractFormulaireData] Vérification de la clé 'reponses'...");
        if (contenu.containsKey("reponses") && contenu.get("reponses") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reponsesMap = (Map<String, Object>) contenu.get("reponses");
            logger.info("[extractFormulaireData] Clé 'reponses' trouvée avec {} clés: {}", reponsesMap.size(), reponsesMap.keySet());
            // Fusionner les réponses dans formulaireData pour que les règles y aient accès
            formulaireData.putAll(reponsesMap);
            logger.info("[extractFormulaireData] Données de 'reponses' extraites et fusionnées dans formulaireData pour les règles");
        } else {
            logger.warn("[extractFormulaireData] Clé 'reponses' non trouvée ou n'est pas une Map. Type: {}", 
                       contenu.containsKey("reponses") ? contenu.get("reponses").getClass().getSimpleName() : "absent");
        }
        
        // Ensuite, ajouter les autres clés (hors métadonnées et "reponses")
        logger.info("[extractFormulaireData] Parcours des autres clés du contenu...");
        int clesAjoutees = 0;
        for (Map.Entry<String, Object> entry : contenu.entrySet()) {
            String key = entry.getKey();
            // Exclure les clés de métadonnées et "reponses" (déjà traité ci-dessus)
            if (!key.equals("fields") && !key.equals("actions") && !key.equals("champs") && !key.equals("reponses")) {
                formulaireData.put(key, entry.getValue());
                clesAjoutees++;
                logger.info("[extractFormulaireData] Clé '{}' ajoutée à formulaireData: {}", key, entry.getValue());
            } else {
                logger.info("[extractFormulaireData] Clé '{}' exclue (métadonnée)", key);
            }
        }
        logger.info("[extractFormulaireData] {} clés ajoutées depuis le contenu (hors métadonnées)", clesAjoutees);
        
        logger.info("[extractFormulaireData] Données du formulaire extraites pour les règles: {} clés totales - {}", 
                   formulaireData.size(), formulaireData.keySet());
        return formulaireData;
    }
}

