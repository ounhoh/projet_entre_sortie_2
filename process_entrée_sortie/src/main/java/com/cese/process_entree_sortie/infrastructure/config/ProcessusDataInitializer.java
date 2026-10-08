package com.cese.process_entree_sortie.infrastructure.config;

import java.util.Map;
import java.util.UUID;

/**
 * Classe abstraite de base pour l'initialisation des données d'un processus.
 * 
 * Chaque processus (entrée_agent, sortie_agent, etc.) aura sa propre implémentation
 * qui étend cette classe et implémente la méthode initialize().
 */
public abstract class ProcessusDataInitializer {
    
    /**
     * Initialise toutes les données nécessaires pour un processus :
     * - Le template processus
     * - Les statuts
     * - Les groupes de tâches
     * - Les tâches
     * - Les dépendances
     * 
     * @param directions Map des directions (code -> UUID) nécessaires pour créer les groupes et tâches
     */
    public abstract void initialize(Map<String, UUID> directions);
    
    /**
     * Retourne le nom du processus (pour le logging).
     * 
     * @return Le nom du processus (ex: "entrée_agent", "sortie_agent")
     */
    public abstract String getProcessusName();
}
