package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.port.out.processus.ProcessusSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service planifié pour vérifier les délais des processus et envoyer des alertes.
 * Exécuté quotidiennement pour vérifier les tâches/groupes en retard ou approchant leur délai.
 */
@Service
@Transactional
public class VerifierDelaisService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerifierDelaisService.class);
    
    private final ProcessusSpi processusSpi;
    private final NotificationService notificationService;
    
    public VerifierDelaisService(ProcessusSpi processusSpi, NotificationService notificationService) {
        this.processusSpi = processusSpi;
        this.notificationService = notificationService;
    }
    
    /**
     * Vérifie les délais de tous les processus actifs.
     * Exécuté quotidiennement à 8h00.
     */
    @Scheduled(cron = "0 0 8 * * ?") // Tous les jours à 8h00
    public void verifierDelaisTousProcessus() {
        logger.info("Démarrage de la vérification des délais pour tous les processus...");
        
        try {
            // Récupérer tous les processus actifs
            List<com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus> processusActifs = processusSpi.findActifs();
            logger.info("Nombre de processus actifs à vérifier: {}", processusActifs.size());
            
            for (var processus : processusActifs) {
                notificationService.verifierEtNotifierDelais(processus.id());
            }
            
            logger.info("Vérification des délais terminée");
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des délais: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie les délais pour un processus spécifique.
     * Peut être appelé manuellement ou depuis un autre service.
     * 
     * @param processusId L'ID du processus à vérifier
     */
    public void verifierDelaisProcessus(UUID processusId) {
        try {
            notificationService.verifierEtNotifierDelais(processusId);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des délais pour le processus {}: {}", 
                       processusId, e.getMessage(), e);
        }
    }
}
