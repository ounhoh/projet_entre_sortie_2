package com.cese.process_entree_sortie.infrastructure.config;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.*;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Initialiseur pour le processus "entrée_agent".
 * 
 * Cette classe gère l'initialisation de toutes les données nécessaires pour le processus "entrée_agent" :
 * - Le template processus
 * - Les statuts
 * - Les groupes de tâches
 * - Les tâches
 * - Les dépendances
 */
@Component
public class EntreeAgentDataInitializer extends ProcessusDataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(EntreeAgentDataInitializer.class);
    
    private final StatutProcessusJpaRepository statutProcessusJpaRepository;
    private final TemplateProcessusJpaRepository templateProcessusJpaRepository;
    private final TemplateGroupeTacheJpaRepository templateGroupeTacheJpaRepository;
    private final TemplateTacheJpaRepository templateTacheJpaRepository;
    private final TemplateGroupeTacheAssociationJpaRepository templateGroupeTacheAssociationJpaRepository;
    private final TemplateDependanceJpaRepository templateDependanceJpaRepository;
    private final DirectionJpaRepository directionJpaRepository;
    private final ObjectMapper objectMapper;
    
    public EntreeAgentDataInitializer(
            StatutProcessusJpaRepository statutProcessusJpaRepository,
            TemplateProcessusJpaRepository templateProcessusJpaRepository,
            TemplateGroupeTacheJpaRepository templateGroupeTacheJpaRepository,
            TemplateTacheJpaRepository templateTacheJpaRepository,
            TemplateGroupeTacheAssociationJpaRepository templateGroupeTacheAssociationJpaRepository,
            TemplateDependanceJpaRepository templateDependanceJpaRepository,
            DirectionJpaRepository directionJpaRepository,
            ObjectMapper objectMapper) {
        this.statutProcessusJpaRepository = statutProcessusJpaRepository;
        this.templateProcessusJpaRepository = templateProcessusJpaRepository;
        this.templateGroupeTacheJpaRepository = templateGroupeTacheJpaRepository;
        this.templateTacheJpaRepository = templateTacheJpaRepository;
        this.templateGroupeTacheAssociationJpaRepository = templateGroupeTacheAssociationJpaRepository;
        this.templateDependanceJpaRepository = templateDependanceJpaRepository;
        this.directionJpaRepository = directionJpaRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getProcessusName() {
        return "entrée_agent";
    }
    
    @Override
    @Transactional
    public void initialize(Map<String, UUID> directions) {
        logger.info("Initialisation du processus '{}'...", getProcessusName());
        
        UUID templateProcessusId = initialiserTemplateProcessus();
        Map<String, UUID> statuts = initialiserStatutsProcessus(templateProcessusId);
        Map<String, UUID> groupes = initialiserGroupesTaches(templateProcessusId, directions, statuts);
        Map<String, UUID> taches = initialiserTaches(groupes);
        initialiserDependances(taches, groupes);
        
        logger.info("Initialisation du processus '{}' terminée.", getProcessusName());
    }
    
    private UUID initialiserTemplateProcessus() {
        Optional<TemplateProcessusEntity> existing = templateProcessusJpaRepository.findByCodeProcessus("entrée_agent_creation");
        
        if (existing.isPresent()) {
            logger.info("Le template processus 'entrée_agent' existe déjà.");
            return existing.get().getId();
        }
        
        logger.info("Création du template processus 'entrée_agent'...");
        
        TemplateProcessusEntity template = new TemplateProcessusEntity(
            UUID.randomUUID(),
            "entrée_agent_creation",
            "Entrée",
            "Processus de création",
            TypeProcessus.entree,
            true
        );
        
        TemplateProcessusEntity saved = templateProcessusJpaRepository.save(template);
        logger.info("Template processus 'entrée_agent' créé avec l'ID: {}", saved.getId());
        
        return saved.getId();
    }
    
    private Map<String, UUID> initialiserStatutsProcessus(UUID templateProcessusId) {
        Map<String, UUID> statutsMap = new HashMap<>();
        
        // Vérifier si les statuts pour ce template existent déjà
        List<StatutProcessusEntity> statutsExistants = statutProcessusJpaRepository.findByTemplateProcessusId(templateProcessusId);
        if (!statutsExistants.isEmpty()) {
            logger.info("Les statuts pour le template 'entrée_agent' existent déjà, récupération des IDs...");
            // Mettre à jour les statuts existants qui n'ont pas d'ordre
            Map<String, Integer> ordreMap = Map.of(
                "Initialisation", 1,
                "Formulaire des droits", 2,
                "Ouverture des droits", 3,
                "Processus cloturé", 4,
                "Archivé", 5
            );
            
            for (StatutProcessusEntity statut : statutsExistants) {
                boolean misAJour = false;
                
                // Renommer "Ouverture des droits et matériels" en "Ouverture des droits" si nécessaire
                if ("Ouverture des droits et matériels".equals(statut.getLibStatut())) {
                    statut.setLibStatut("Ouverture des droits");
                    misAJour = true;
                    logger.info("Statut 'Ouverture des droits et matériels' renommé en 'Ouverture des droits'");
                }
                
                // Mettre à jour l'ordre si manquant
                Integer ordreAttendu = ordreMap.get(statut.getLibStatut());
                if (ordreAttendu != null && statut.getOrdre() == null) {
                    statut.setOrdre(ordreAttendu);
                    misAJour = true;
                    logger.info("Statut '{}' mis à jour avec l'ordre: {}", statut.getLibStatut(), ordreAttendu);
                }
                
                // Mettre à jour le template_processus_id si manquant
                if (statut.getTemplateProcessusId() == null) {
                    statut.setTemplateProcessusId(templateProcessusId);
                    misAJour = true;
                    logger.info("Statut '{}' mis à jour avec le template_processus_id: {}", statut.getLibStatut(), templateProcessusId);
                }
                
                // Mettre à jour le codeStatut du premier statut (Initialisation) pour qu'il soit 'en_attente'
                if ("Initialisation".equals(statut.getLibStatut()) && 
                    !"en_attente".equals(statut.getCodeStatut())) {
                    String ancienCodeStatut = statut.getCodeStatut();
                    statut.setCodeStatut("en_attente");
                    misAJour = true;
                    logger.info("Statut '{}' mis à jour avec le codeStatut: 'en_attente' (ancien: '{}')", 
                        statut.getLibStatut(), ancienCodeStatut);
                }
                
                if (misAJour) {
                    statutProcessusJpaRepository.save(statut);
                }
                
                // Utiliser le nom correct pour la clé de la Map (après le renommage éventuel)
                statutsMap.put(statut.getLibStatut(), statut.getId());
            }

            if (!statutsMap.containsKey("Archivé")) {
                StatutProcessusEntity statutArchive = new StatutProcessusEntity(
                    UUID.randomUUID(),
                    "archive",
                    "Archivé",
                    "processus terminé et archivé",
                    5,
                    templateProcessusId
                );
                statutProcessusJpaRepository.save(statutArchive);
                statutsMap.put("Archivé", statutArchive.getId());
                logger.info("Statut 'Archivé' créé (ordre: 5)");
            }
            return statutsMap;
        }
        
        // Créer les statuts pour ce template
        logger.info("Création des statuts de processus pour le template 'entrée_agent'...");
        
        StatutProcessusEntity statut1 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "en_attente",
            "Initialisation",
            "étape du formulaire de création d'un agent pour remplir ses informations personnelles",
            1,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut1);
        statutsMap.put("Initialisation", statut1.getId());
        logger.info("Statut 'Initialisation' créé (ordre: 1)");
        
        StatutProcessusEntity statut2 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_entree_agent_2",
            "Formulaire des droits",
            "étape du formulaire de création d'un agent pour remplir ses droits",
            2,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut2);
        statutsMap.put("Formulaire des droits", statut2.getId());
        logger.info("Statut 'Formulaire des droits' créé (ordre: 2)");
        
        StatutProcessusEntity statut3 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_entree_agent_3",
            "Ouverture des droits",
            "étape de réalisation des taches de création d'un agent",
            3,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut3);
        statutsMap.put("Ouverture des droits", statut3.getId());
        logger.info("Statut 'Ouverture des droits' créé (ordre: 3)");
        
        StatutProcessusEntity statut4 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_entree_agent_4",
            "Processus cloturé",
            "étape de fin de création d'un agent",
            4,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut4);
        statutsMap.put("Processus cloturé", statut4.getId());
        logger.info("Statut 'Processus cloturé' créé (ordre: 4)");

        StatutProcessusEntity statut5 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "archive",
            "Archivé",
            "processus terminé et archivé",
            5,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut5);
        statutsMap.put("Archivé", statut5.getId());
        logger.info("Statut 'Archivé' créé (ordre: 5)");
        
        return statutsMap;
    }
    
    private Map<String, UUID> initialiserGroupesTaches(UUID templateProcessusId, Map<String, UUID> directions, Map<String, UUID> statuts) {
        Map<String, UUID> groupesMap = new HashMap<>();
        
        // Vérifier que toutes les directions nécessaires sont présentes
        UUID drhId = directions.get("DRH");
        UUID dsiunId = directions.get("DSIUN");
        UUID dappiId = directions.get("DAPPI");
        UUID dafId = directions.get("DAF");
        UUID diciId = directions.get("DICI");
        
        if (drhId == null) {
            logger.error("Direction DRH non trouvée ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Direction DRH est requise mais non trouvée dans la base de données.");
        }
        if (dsiunId == null) {
            logger.error("Direction DSIUN non trouvée ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Direction DSIUN est requise mais non trouvée dans la base de données.");
        }
        if (dappiId == null) {
            logger.error("Direction DAPPI non trouvée ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Direction DAPPI est requise mais non trouvée dans la base de données.");
        }
        if (dafId == null) {
            logger.error("Direction DAF non trouvée ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Direction DAF est requise mais non trouvée dans la base de données.");
        }
        if (diciId == null) {
            logger.error("Direction DICI non trouvée ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Direction DICI est requise mais non trouvée dans la base de données.");
        }
        
        // Récupérer les codes de direction depuis les IDs
        String codeDirectionDRH = directionJpaRepository.findById(drhId)
            .map(DirectionEntity::getCodeDirection)
            .orElse(null);
        String codeDirectionDSIUN = directionJpaRepository.findById(dsiunId)
            .map(DirectionEntity::getCodeDirection)
            .orElse(null);
        String codeDirectionDAPPI = directionJpaRepository.findById(dappiId)
            .map(DirectionEntity::getCodeDirection)
            .orElse(null);
        String codeDirectionDAF = directionJpaRepository.findById(dafId)
            .map(DirectionEntity::getCodeDirection)
            .orElse(null);
        String codeDirectionDICI = directionJpaRepository.findById(diciId)
            .map(DirectionEntity::getCodeDirection)
            .orElse(null);
        
        // Vérifier que tous les statuts nécessaires sont présents
        UUID statutInit = statuts.get("Initialisation");
        UUID statutFormulaire = statuts.get("Formulaire des droits");
        UUID statutOuverture = statuts.get("Ouverture des droits");
        
        if (statutInit == null || statutFormulaire == null || statutOuverture == null) {
            logger.error("Statuts de processus manquants ! Impossible de créer les groupes de tâches.");
            throw new IllegalStateException("Tous les statuts de processus sont requis mais certains sont manquants.");
        }
        
        // Groupe 1: form_agent_creation_rh
        Optional<TemplateGroupeTacheEntity> groupe1 = templateGroupeTacheJpaRepository.findByCodeTemplate("form_agent_creation_rh");
        if (groupe1.isEmpty()) {
            TemplateGroupeTacheEntity grp1 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "form_agent_creation_rh",
                codeDirectionDRH,
                "Formulaire de création",
                statutInit,
                drhId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp1);
            groupesMap.put("form_agent_creation_rh", grp1.getId());
            logger.info("Groupe de tâches 'form_agent_creation_rh' créé avec codeDirection: {}", codeDirectionDRH);
        } else {
            TemplateGroupeTacheEntity grp1 = groupe1.get();
            boolean misAJour = false;
            if (grp1.getCodeDirection() == null || !grp1.getCodeDirection().equals(codeDirectionDRH)) {
                grp1.setCodeDirection(codeDirectionDRH);
                misAJour = true;
            }
            if (grp1.getTemplateProcessusId() == null || !grp1.getTemplateProcessusId().equals(templateProcessusId)) {
                grp1.setTemplateProcessusId(templateProcessusId);
                misAJour = true;
            }
            if (grp1.getStatutProcessusId() == null || !grp1.getStatutProcessusId().equals(statutInit)) {
                grp1.setStatutProcessusId(statutInit);
                misAJour = true;
            }
            if (grp1.isDirectionConcernee() != false) {
                grp1.setDirectionConcernee(false);
                misAJour = true;
            }
            if (misAJour) {
                templateGroupeTacheJpaRepository.save(grp1);
            }
            groupesMap.put("form_agent_creation_rh", grp1.getId());
        }
        
        // Groupe 2: agent_form_concenrnee
        Optional<TemplateGroupeTacheEntity> groupe2 = templateGroupeTacheJpaRepository.findByCodeTemplate("agent_form_concenrnee");
        if (groupe2.isEmpty()) {
            TemplateGroupeTacheEntity grp2 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "agent_form_concenrnee",
                codeDirectionDRH,
                "Groupe de tâches de création",
                statutFormulaire,
                drhId,
                templateProcessusId,
                true
            );
            templateGroupeTacheJpaRepository.save(grp2);
            groupesMap.put("agent_form_concenrnee", grp2.getId());
        } else {
            TemplateGroupeTacheEntity grp2 = groupe2.get();
            boolean misAJour = false;
            if (grp2.getCodeDirection() == null || !grp2.getCodeDirection().equals(codeDirectionDRH)) {
                grp2.setCodeDirection(codeDirectionDRH);
                misAJour = true;
            }
            if (grp2.getTemplateProcessusId() == null || !grp2.getTemplateProcessusId().equals(templateProcessusId)) {
                grp2.setTemplateProcessusId(templateProcessusId);
                misAJour = true;
            }
            if (grp2.getStatutProcessusId() == null || !grp2.getStatutProcessusId().equals(statutFormulaire)) {
                grp2.setStatutProcessusId(statutFormulaire);
                misAJour = true;
            }
            if (grp2.isDirectionConcernee() != true) {
                grp2.setDirectionConcernee(true);
                misAJour = true;
            }
            if (misAJour) {
                templateGroupeTacheJpaRepository.save(grp2);
            }
            groupesMap.put("agent_form_concenrnee", grp2.getId());
        }
        
        // Groupe 3: virtualia_creation
        Optional<TemplateGroupeTacheEntity> groupe3 = templateGroupeTacheJpaRepository.findByCodeTemplate("virtualia_creation");
        if (groupe3.isEmpty()) {
            TemplateGroupeTacheEntity grp3 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "virtualia_creation",
                codeDirectionDRH,
                "Virtualia et Organigramme",
                statutOuverture,
                drhId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp3);
            groupesMap.put("virtualia_creation", grp3.getId());
        } else {
            TemplateGroupeTacheEntity grp3 = groupe3.get();
            boolean misAJour = false;
            if (grp3.getCodeDirection() == null || !grp3.getCodeDirection().equals(codeDirectionDRH)) {
                grp3.setCodeDirection(codeDirectionDRH);
                misAJour = true;
            }
            if (grp3.getDirectionId() == null || !grp3.getDirectionId().equals(drhId)) {
                grp3.setDirectionId(drhId);
                misAJour = true;
            }
            if (grp3.getTemplateProcessusId() == null || !grp3.getTemplateProcessusId().equals(templateProcessusId)) {
                grp3.setTemplateProcessusId(templateProcessusId);
                misAJour = true;
            }
            if (grp3.getStatutProcessusId() == null || !grp3.getStatutProcessusId().equals(statutOuverture)) {
                grp3.setStatutProcessusId(statutOuverture);
                misAJour = true;
            }
            if (grp3.isDirectionConcernee() != false) {
                grp3.setDirectionConcernee(false);
                misAJour = true;
            }
            if (misAJour) {
                templateGroupeTacheJpaRepository.save(grp3);
            }
            groupesMap.put("virtualia_creation", grp3.getId());
        }
        
        // Groupe 4: organigramme_ajout_dici (DICI)
        Optional<TemplateGroupeTacheEntity> groupe4dici = templateGroupeTacheJpaRepository.findByCodeTemplate("organigramme_ajout_dici");
        if (groupe4dici.isEmpty()) {
            TemplateGroupeTacheEntity grp4dici = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "organigramme_ajout_dici",
                codeDirectionDICI,
                "Ajouter dans l'organigramme",
                statutOuverture,
                diciId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp4dici);
            groupesMap.put("organigramme_ajout_dici", grp4dici.getId());
        } else {
            groupesMap.put("organigramme_ajout_dici", groupe4dici.get().getId());
        }
        
        // Groupe 5: badge_creation
        Optional<TemplateGroupeTacheEntity> groupe4 = templateGroupeTacheJpaRepository.findByCodeTemplate("badge_creation");
        if (groupe4.isEmpty()) {
            TemplateGroupeTacheEntity grp4 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "badge_creation",
                codeDirectionDAPPI,
                "Création du badge",
                statutOuverture,
                dappiId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp4);
            groupesMap.put("badge_creation", grp4.getId());
        } else {
            TemplateGroupeTacheEntity grp4 = groupe4.get();
            boolean misAJour = false;
            if (grp4.getCodeDirection() == null || !grp4.getCodeDirection().equals(codeDirectionDAPPI)) {
                grp4.setCodeDirection(codeDirectionDAPPI);
                misAJour = true;
            }
            if (grp4.getTemplateProcessusId() == null || !grp4.getTemplateProcessusId().equals(templateProcessusId)) {
                grp4.setTemplateProcessusId(templateProcessusId);
                misAJour = true;
            }
            if (grp4.getStatutProcessusId() == null || !grp4.getStatutProcessusId().equals(statutOuverture)) {
                grp4.setStatutProcessusId(statutOuverture);
                misAJour = true;
            }
            if (grp4.isDirectionConcernee() != false) {
                grp4.setDirectionConcernee(false);
                misAJour = true;
            }
            if (misAJour) {
                templateGroupeTacheJpaRepository.save(grp4);
            }
            groupesMap.put("badge_creation", grp4.getId());
        }
        
        // Groupe 5: cantine_droits
        Optional<TemplateGroupeTacheEntity> groupe5 = templateGroupeTacheJpaRepository.findByCodeTemplate("cantine_droits");
        if (groupe5.isEmpty()) {
            TemplateGroupeTacheEntity grp5 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "cantine_droits",
                codeDirectionDAF,
                "Droits de la cantine",
                statutOuverture,
                dafId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp5);
            groupesMap.put("cantine_droits", grp5.getId());
        } else {
            groupesMap.put("cantine_droits", groupe5.get().getId());
        }
        
        // Groupe 6: agent_paye
        Optional<TemplateGroupeTacheEntity> groupe6 = templateGroupeTacheJpaRepository.findByCodeTemplate("agent_paye");
        if (groupe6.isEmpty()) {
            TemplateGroupeTacheEntity grp6 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "agent_paye",
                codeDirectionDAF,
                "Rémunération",
                statutOuverture,
                dafId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp6);
            groupesMap.put("agent_paye", grp6.getId());
        } else {
            groupesMap.put("agent_paye", groupe6.get().getId());
        }
        
        // Groupe 7: adresse_mail_creation
        Optional<TemplateGroupeTacheEntity> groupe7 = templateGroupeTacheJpaRepository.findByCodeTemplate("adresse_mail_creation");
        if (groupe7.isEmpty()) {
            TemplateGroupeTacheEntity grp7 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "adresse_mail_creation",
                codeDirectionDSIUN,
                "Création de l'adresse mail",
                statutOuverture,
                dsiunId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp7);
            groupesMap.put("adresse_mail_creation", grp7.getId());
        } else {
            groupesMap.put("adresse_mail_creation", groupe7.get().getId());
        }
         
        // Groupe 8: materiel_agent_creation
        Optional<TemplateGroupeTacheEntity> groupe8 = templateGroupeTacheJpaRepository.findByCodeTemplate("materiel_agent_creation");
        if (groupe8.isEmpty()) {
            TemplateGroupeTacheEntity grp8 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "materiel_agent_creation",
                codeDirectionDSIUN,
                "Matériel",
                statutOuverture,
                dsiunId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp8);
            groupesMap.put("materiel_agent_creation", grp8.getId());
        } else {
            groupesMap.put("materiel_agent_creation", groupe8.get().getId());
        }
        // groupe 9: numero_bureau_creation
        Optional<TemplateGroupeTacheEntity> groupe9 = templateGroupeTacheJpaRepository.findByCodeTemplate("numero_bureau_creation");
        if (groupe9.isEmpty()) {
            TemplateGroupeTacheEntity grp9 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "numero_bureau_creation",
                codeDirectionDAPPI,
                "Clé du bureau à donner et bureau aménagé",
                statutOuverture,
                dappiId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp9);
            groupesMap.put("numero_bureau_creation", grp9.getId());
        } else {
            groupesMap.put("numero_bureau_creation", groupe9.get().getId());
        }
        
        // Groupe 10: cantine_droits_drh (DRH)
        Optional<TemplateGroupeTacheEntity> groupe10 = templateGroupeTacheJpaRepository.findByCodeTemplate("cantine_droits_drh");
        if (groupe10.isEmpty()) {
            TemplateGroupeTacheEntity grp10 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "cantine_droits_drh",
                codeDirectionDRH,
                "Droits de la cantine",
                statutOuverture,
                drhId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp10);
            groupesMap.put("cantine_droits_drh", grp10.getId());
        } else {
            groupesMap.put("cantine_droits_drh", groupe10.get().getId());
        }
        
        // Groupe 11: agent_paye_drh (DRH)
        Optional<TemplateGroupeTacheEntity> groupe11 = templateGroupeTacheJpaRepository.findByCodeTemplate("agent_paye_drh");
        if (groupe11.isEmpty()) {
            TemplateGroupeTacheEntity grp11 = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                "agent_paye_drh",
                codeDirectionDRH,
                "Rémunération",
                statutOuverture,
                drhId,
                templateProcessusId,
                false
            );
            templateGroupeTacheJpaRepository.save(grp11);
            groupesMap.put("agent_paye_drh", grp11.getId());
        } else {
            groupesMap.put("agent_paye_drh", groupe11.get().getId());
        }
        
        return groupesMap;
    }
    
    /**
     * Initialise les tâches et leurs associations avec les groupes.
     * Retourne une Map avec le code de la tâche comme clé et l'UUID comme valeur.
     */
    private Map<String, UUID> initialiserTaches(Map<String, UUID> groupes) {
        Map<String, UUID> tachesMap = new HashMap<>();
        
        // Tâche 1: form_agent_creation_rh (dans groupe form_agent_creation_rh)
        Optional<TemplateTacheEntity> tache1 = templateTacheJpaRepository.findByCode("form_agent_creation_rh");
        UUID tache1Id;
        if (tache1.isEmpty()) {
            String contenu1 = createFormulaireRHContenu();
            TemplateTacheEntity t1 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "form_agent_creation_rh",
                "Formulaire de création",
                "Tâche de création",
                TacheType.formulaire,
                0,
                contenu1,
                null
            );
            templateTacheJpaRepository.save(t1);
            tache1Id = t1.getId();
            tachesMap.put("form_agent_creation_rh", tache1Id);
        } else {
            TemplateTacheEntity t1 = tache1.get();
            boolean doitMettreAJour = false;
            if (t1.getContenu() == null || t1.getContenu().isEmpty() || 
                t1.getContenu().trim().equals("[]") || t1.getContenu().trim().equals("{}") ||
                (!t1.getContenu().trim().startsWith("[") && !t1.getContenu().trim().startsWith("{"))) {
                doitMettreAJour = true;
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contenuMap = (Map<String, Object>) objectMapper.readValue(t1.getContenu(), Map.class);
                    if (!contenuMap.containsKey("actions")) {
                        doitMettreAJour = true;
                    }
                } catch (Exception e) {
                    doitMettreAJour = true;
                }
            }
            if (doitMettreAJour) {
                String contenu1 = createFormulaireRHContenu();
                t1.setContenu(contenu1);
                templateTacheJpaRepository.save(t1);
            }
            tache1Id = t1.getId();
            tachesMap.put("form_agent_creation_rh", tache1Id);
        }
        
        UUID groupe1Id = groupes.get("form_agent_creation_rh");
        if (!templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe1Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache1Id))) {
            TemplateGroupeTacheAssociationEntity assoc1 = new TemplateGroupeTacheAssociationEntity(
                groupe1Id,
                tache1Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc1);
        }
        
        // Tâche 2: form_agent_creation_concernee (dans groupe agent_form_concenrnee)
        Optional<TemplateTacheEntity> tache2 = templateTacheJpaRepository.findByCode("form_agent_creation_concernee");
        UUID tache2Id;
        if (tache2.isEmpty()) {
            String contenu2 = createFormulaireConcerneeContenu();
            TemplateTacheEntity t2 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "form_agent_creation_concernee",
                "Formulaire de création pour la direction concernée",
                "Tâche de création pour la direction concernée",
                TacheType.formulaire,
                0,
                contenu2,
                null
            );
            templateTacheJpaRepository.save(t2);
            tache2Id = t2.getId();
            tachesMap.put("form_agent_creation_concernee", tache2Id);
        } else {
            TemplateTacheEntity t2 = tache2.get();
            boolean doitMettreAJour = false;
            if (t2.getContenu() == null || t2.getContenu().isEmpty() || 
                t2.getContenu().trim().equals("[]") || t2.getContenu().trim().equals("{}") ||
                (!t2.getContenu().trim().startsWith("[") && !t2.getContenu().trim().startsWith("{"))) {
                doitMettreAJour = true;
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contenuMap = (Map<String, Object>) objectMapper.readValue(t2.getContenu(), Map.class);
                    if (!contenuMap.containsKey("actions")) {
                        doitMettreAJour = true;
                    }
                } catch (Exception e) {
                    doitMettreAJour = true;
                }
            }
            if (doitMettreAJour) {
                String contenu2 = createFormulaireConcerneeContenu();
                t2.setContenu(contenu2);
                templateTacheJpaRepository.save(t2);
            }
            tache2Id = t2.getId();
            tachesMap.put("form_agent_creation_concernee", tache2Id);
        }
        
        UUID groupe2Id = groupes.get("agent_form_concenrnee");
        if (!templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe2Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache2Id))) {
            TemplateGroupeTacheAssociationEntity assoc2 = new TemplateGroupeTacheAssociationEntity(
                groupe2Id,
                tache2Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc2);
        }
        
        // Tâche 3: virtualia_creation
        Optional<TemplateTacheEntity> tache3 = templateTacheJpaRepository.findByCode("virtualia_creation");
        UUID tache3Id;
        if (tache3.isEmpty()) {
            TemplateTacheEntity t3 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "virtualia_creation",
                "Créer un compte virtualia",
                "Créer un compte virtualia",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t3);
            tache3Id = t3.getId();
            tachesMap.put("virtualia_creation", tache3Id);
        } else {
            tache3Id = tache3.get().getId();
            tachesMap.put("virtualia_creation", tache3Id);
        }
        
        UUID groupe3Id = groupes.get("virtualia_creation");
        if (!templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe3Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache3Id))) {
            TemplateGroupeTacheAssociationEntity assoc3 = new TemplateGroupeTacheAssociationEntity(
                groupe3Id,
                tache3Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc3);
        }
        
        // Tâche 4: organigramme_ajout
        Optional<TemplateTacheEntity> tache4 = templateTacheJpaRepository.findByCode("organigramme_ajout");
        UUID tache4Id;
        if (tache4.isEmpty()) {
            TemplateTacheEntity t4 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "organigramme_ajout",
                "Ajouter dans l'organigramme",
                "Ajouter dans l'organigramme",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t4);
            tache4Id = t4.getId();
            tachesMap.put("organigramme_ajout", tache4Id);
        } else {
            tache4Id = tache4.get().getId();
            tachesMap.put("organigramme_ajout", tache4Id);
        }
        
        if (!templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe3Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache4Id))) {
            TemplateGroupeTacheAssociationEntity assoc4 = new TemplateGroupeTacheAssociationEntity(
                groupe3Id,
                tache4Id,
                2
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc4);
        }
        
        // Tâche 4b: organigramme_ajout_dici (pour DICI)
        Optional<TemplateTacheEntity> tache4dici = templateTacheJpaRepository.findByCode("organigramme_ajout_dici");
        UUID tache4diciId;
        if (tache4dici.isEmpty()) {
            TemplateTacheEntity t4dici = new TemplateTacheEntity(
                UUID.randomUUID(),
                "organigramme_ajout_dici",
                "Ajouter dans l'organigramme",
                "Ajouter dans l'organigramme",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t4dici);
            tache4diciId = t4dici.getId();
            tachesMap.put("organigramme_ajout_dici", tache4diciId);
        } else {
            tache4diciId = tache4dici.get().getId();
            tachesMap.put("organigramme_ajout_dici", tache4diciId);
        }
        
        UUID groupe4diciId = groupes.get("organigramme_ajout_dici");
        if (groupe4diciId != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe4diciId).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache4diciId))) {
            TemplateGroupeTacheAssociationEntity assoc4dici = new TemplateGroupeTacheAssociationEntity(
                groupe4diciId,
                tache4diciId,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc4dici);
        }
        
        // Tâche 5: badge_creation
        Optional<TemplateTacheEntity> tache5 = templateTacheJpaRepository.findByCode("badge_creation");
        UUID tache5Id;
        if (tache5.isEmpty()) {
            TemplateTacheEntity t5 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "badge_creation",
                "Création du badge",
                "Création du badge",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t5);
            tache5Id = t5.getId();
            tachesMap.put("badge_creation", tache5Id);
        } else {
            tache5Id = tache5.get().getId();
            tachesMap.put("badge_creation", tache5Id);
        }
        
        UUID groupe4Id = groupes.get("badge_creation");
        if (!templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe4Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache5Id))) {
            TemplateGroupeTacheAssociationEntity assoc5 = new TemplateGroupeTacheAssociationEntity(
                groupe4Id,
                tache5Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc5);
        }
        
        // Tâche 6: cantine_droits_creation
        Optional<TemplateTacheEntity> tache6 = templateTacheJpaRepository.findByCode("cantine_droits_creation");
        UUID tache6Id;
        if (tache6.isEmpty()) {
            TemplateTacheEntity t6 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "cantine_droits_creation",
                "Droits de la cantine",
                "Attribuer les droits à la cantine",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t6);
            tache6Id = t6.getId();
            tachesMap.put("cantine_droits_creation", tache6Id);
        } else {
            tache6Id = tache6.get().getId();
            tachesMap.put("cantine_droits_creation", tache6Id);
        }
        
        UUID groupe5Id = groupes.get("cantine_droits");
        if (groupe5Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe5Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache6Id))) {
            TemplateGroupeTacheAssociationEntity assoc6 = new TemplateGroupeTacheAssociationEntity(
                groupe5Id,
                tache6Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc6);
        }
        
        // Tâche 7: paye_creation
        Optional<TemplateTacheEntity> tache7 = templateTacheJpaRepository.findByCode("paye_creation");
        UUID tache7Id;
        if (tache7.isEmpty()) {
            TemplateTacheEntity t7 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "paye_creation",
                "Création de la paye",
                "Traitement de la paye",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t7);
            tache7Id = t7.getId();
            tachesMap.put("paye_creation", tache7Id);
        } else {
            tache7Id = tache7.get().getId();
            tachesMap.put("paye_creation", tache7Id);
        }
        
        UUID groupe6Id = groupes.get("agent_paye");
        if (groupe6Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe6Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache7Id))) {
            TemplateGroupeTacheAssociationEntity assoc7 = new TemplateGroupeTacheAssociationEntity(
                groupe6Id,
                tache7Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc7);
        }
        
        // Tâche 8: cantine_droits_creation_drh (pour la DRH)
        Optional<TemplateTacheEntity> tache8drh = templateTacheJpaRepository.findByCode("cantine_droits_creation_drh");
        UUID tache8drhId;
        if (tache8drh.isEmpty()) {
            TemplateTacheEntity t8drh = new TemplateTacheEntity(
                UUID.randomUUID(),
                "cantine_droits_creation_drh",
                "Droits de la cantine",
                "Attribuer les droits à la cantine",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t8drh);
            tache8drhId = t8drh.getId();
            tachesMap.put("cantine_droits_creation_drh", tache8drhId);
        } else {
            tache8drhId = tache8drh.get().getId();
            tachesMap.put("cantine_droits_creation_drh", tache8drhId);
        }
        
        UUID groupe10Id = groupes.get("cantine_droits_drh");
        if (groupe10Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe10Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache8drhId))) {
            TemplateGroupeTacheAssociationEntity assoc10 = new TemplateGroupeTacheAssociationEntity(
                groupe10Id,
                tache8drhId,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc10);
        }
        
        // Tâche 9: paye_creation_drh (pour la DRH)
        Optional<TemplateTacheEntity> tache9drh = templateTacheJpaRepository.findByCode("paye_creation_drh");
        UUID tache9drhId;
        if (tache9drh.isEmpty()) {
            TemplateTacheEntity t9drh = new TemplateTacheEntity(
                UUID.randomUUID(),
                "paye_creation_drh",
                "Création de la paye",
                "Traitement de la paye",
                TacheType.tache,
                7,
                null,
                null
            );
            templateTacheJpaRepository.save(t9drh);
            tache9drhId = t9drh.getId();
            tachesMap.put("paye_creation_drh", tache9drhId);
        } else {
            tache9drhId = tache9drh.get().getId();
            tachesMap.put("paye_creation_drh", tache9drhId);
        }
        
        UUID groupe11Id = groupes.get("agent_paye_drh");
        if (groupe11Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe11Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache9drhId))) {
            TemplateGroupeTacheAssociationEntity assoc11 = new TemplateGroupeTacheAssociationEntity(
                groupe11Id,
                tache9drhId,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc11);
        }
        
        // Tâche 10: adresse_mail_creation
        Optional<TemplateTacheEntity> tache10 = templateTacheJpaRepository.findByCode("adresse_mail_creation");
        UUID tache10Id;
        if (tache10.isEmpty()) {
            String contenu10 = createAdresseMailContenu();
            TemplateTacheEntity t10 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "adresse_mail_creation",
                "Création de l'adresse mail",
                "Création de l'adresse mail",
                TacheType.tache,
                7,
                contenu10,
                null
            );
            templateTacheJpaRepository.save(t10);
            tache10Id = t10.getId();
            tachesMap.put("adresse_mail_creation", tache10Id);
        } else {
            tache10Id = tache10.get().getId();
            tachesMap.put("adresse_mail_creation", tache10Id);
        }
        
        UUID groupe7Id = groupes.get("adresse_mail_creation");
        if (groupe7Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe7Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache10Id))) {
            TemplateGroupeTacheAssociationEntity assoc10_mail = new TemplateGroupeTacheAssociationEntity(
                groupe7Id,
                tache10Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc10_mail);
        }
        
        // Tâche 11: droit_agent_creation
        Optional<TemplateTacheEntity> tache11 = templateTacheJpaRepository.findByCode("droit_agent_creation");
        UUID tache11Id;
        if (tache11.isEmpty()) {
            String contenu11 = createDroitAgentContenu();
            TemplateTacheEntity t11 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "droit_agent_creation",
                "Droits",
                "Attribuer les droits",
                TacheType.tache,
                7,
                contenu11,
                null
            );
            templateTacheJpaRepository.save(t11);
            tache11Id = t11.getId();
            tachesMap.put("droit_agent_creation", tache11Id);
        } else {
            tache11Id = tache11.get().getId();
            String contenu11 = createDroitAgentContenu();
            if (contenu11 != null && !contenu11.equals(tache11.get().getContenu())) {
                TemplateTacheEntity tache = tache11.get();
                tache.setContenu(contenu11);
                templateTacheJpaRepository.save(tache);
            }
            tachesMap.put("droit_agent_creation", tache11Id);
        }
        
        if (groupe7Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe7Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache11Id))) {
            TemplateGroupeTacheAssociationEntity assoc11_droit = new TemplateGroupeTacheAssociationEntity(
                groupe7Id,
                tache11Id,
                2
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc11_droit);
        }
        
        // Tâche 12: materiel_agent_creation
        Optional<TemplateTacheEntity> tache12 = templateTacheJpaRepository.findByCode("materiel_agent_creation");
        UUID tache12Id;
        if (tache12.isEmpty()) {
            String contenu12 = createMaterielAgentContenu();
            TemplateTacheEntity t12 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "materiel_agent_creation",
                "Matériel",
                "Donner le matériel",
                TacheType.tache,
                7,
                contenu12,
                null
            );
            templateTacheJpaRepository.save(t12);
            tache12Id = t12.getId();
            tachesMap.put("materiel_agent_creation", tache12Id);
        } else {
            tache12Id = tache12.get().getId();
            String contenu12 = createMaterielAgentContenu();
            if (contenu12 != null && !contenu12.equals(tache12.get().getContenu())) {
                TemplateTacheEntity tache = tache12.get();
                tache.setContenu(contenu12);
                templateTacheJpaRepository.save(tache);
            }
            tachesMap.put("materiel_agent_creation", tache12Id);
        }
        
        UUID groupe8Id = groupes.get("materiel_agent_creation");
        if (groupe8Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe8Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache12Id))) {
            TemplateGroupeTacheAssociationEntity assoc12 = new TemplateGroupeTacheAssociationEntity(
                groupe8Id,
                tache12Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc12);
        }
        
        // Tâche 13: numero_bureau_creation
        Optional<TemplateTacheEntity> tache13 = templateTacheJpaRepository.findByCode("numero_bureau_creation");
        UUID tache13Id;
        if (tache13.isEmpty()) {
            String contenu13 = createNumeroBureauContenu();
            TemplateTacheEntity t13 = new TemplateTacheEntity(
                UUID.randomUUID(),
                "numero_bureau_creation",
                "Clé du bureau à donner et bureau aménagé",
                "Donner le numéro de bureau",
                TacheType.tache,
                7,
                contenu13,
                null
            );
            templateTacheJpaRepository.save(t13);
            tache13Id = t13.getId();
            tachesMap.put("numero_bureau_creation", tache13Id);
        } else {
            tache13Id = tache13.get().getId();
            tachesMap.put("numero_bureau_creation", tache13Id);
        }
        
        UUID groupe9Id = groupes.get("numero_bureau_creation");
        if (groupe9Id != null && !templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupe9Id).stream()
                .anyMatch(a -> a.getTemplateTacheId().equals(tache13Id))) {
            TemplateGroupeTacheAssociationEntity assoc13 = new TemplateGroupeTacheAssociationEntity(
                groupe9Id,
                tache13Id,
                1
            );
            templateGroupeTacheAssociationJpaRepository.save(assoc13);
        }
        
        mettreAJourTypeTaches();
        
        return tachesMap;
    }
    
    private void mettreAJourTypeTaches() {
        UUID tacheId1 = UUID.fromString("b4063d65-bd05-4f57-a7e6-cbeba0718624");
        templateTacheJpaRepository.findById(tacheId1).ifPresent(tache -> {
            if (tache.getType() != TacheType.formulaire) {
                tache.setType(TacheType.formulaire);
                templateTacheJpaRepository.save(tache);
            }
        });
        UUID tacheId2 = UUID.fromString("b0a9c561-504a-42c8-8b83-9bf8de5d979f");
        templateTacheJpaRepository.findById(tacheId2).ifPresent(tache -> {
            if (tache.getType() != TacheType.formulaire) {
                tache.setType(TacheType.formulaire);
                templateTacheJpaRepository.save(tache);
            }
        });
    }
    
    private void initialiserDependances(Map<String, UUID> taches, Map<String, UUID> groupes) {
        UUID sourceTacheId = taches.get("virtualia_creation");
        UUID cibleTacheId1 = taches.get("organigramme_ajout");
        UUID cibleTacheId2 = taches.get("badge_creation");
        
        if (sourceTacheId == null || cibleTacheId1 == null || cibleTacheId2 == null) {
            logger.error("Impossible de créer la dépendance : certaines tâches sont manquantes");
            return;
        }
        
        // Charger toutes les dépendances avec leurs cibles dans une transaction
        List<TemplateDependanceEntity> allDependances = templateDependanceJpaRepository.findAll();
        // Charger explicitement les collections cibles pour éviter LazyInitializationException
        allDependances.forEach(d -> {
            // Accéder à la collection pour la charger dans la session
            d.getCibles().size();
        });
        
        boolean exists = allDependances.stream()
            .anyMatch(d -> {
                if (d.getSourceTacheId() == null || !d.getSourceTacheId().equals(sourceTacheId)) {
                    return false;
                }
                List<TemplateDependanceCibleEntity> cibles = d.getCibles();
                if (cibles == null || cibles.isEmpty()) {
                    return false;
                }
                boolean hasCible1 = false;
                boolean hasCible2 = false;
                for (TemplateDependanceCibleEntity cible : cibles) {
                    if (cible.getCibleTacheId().equals(cibleTacheId1)) {
                        hasCible1 = true;
                    }
                    if (cible.getCibleTacheId().equals(cibleTacheId2)) {
                        hasCible2 = true;
                    }
                    if (hasCible1 && hasCible2) {
                        return true;
                    }
                }
                return hasCible1 && hasCible2;
            });
        
        if (!exists) {
            TemplateDependanceEntity dependance = new TemplateDependanceEntity(
                UUID.randomUUID(),
                sourceTacheId
            );
            
            TemplateDependanceCibleEntity cible1 = new TemplateDependanceCibleEntity(dependance, cibleTacheId1);
            TemplateDependanceCibleEntity cible2 = new TemplateDependanceCibleEntity(dependance, cibleTacheId2);
            dependance.getCibles().add(cible1);
            dependance.getCibles().add(cible2);
            
            templateDependanceJpaRepository.save(dependance);
            
            templateTacheJpaRepository.findById(cibleTacheId1).ifPresent(t -> {
                t.setDependanceId(dependance.getId());
                templateTacheJpaRepository.save(t);
            });
            templateTacheJpaRepository.findById(cibleTacheId2).ifPresent(t -> {
                t.setDependanceId(dependance.getId());
                templateTacheJpaRepository.save(t);
            });
        }
        
        creerDependance(taches, "form_agent_creation_rh", "form_agent_creation_concernee");
        creerDependance(taches, "form_agent_creation_concernee", "virtualia_creation");
        creerDependance(taches, "form_agent_creation_concernee", "paye_creation");
        creerDependance(taches, "badge_creation", "cantine_droits_creation");
        creerDependance(taches, "form_agent_creation_concernee", "adresse_mail_creation");
        creerDependance(taches, "adresse_mail_creation", "droit_agent_creation");
        creerDependance(taches, "form_agent_creation_concernee", "materiel_agent_creation");
        creerDependance(taches, "form_agent_creation_concernee", "numero_bureau_creation");
        
        // Dépendances pour les tâches DRH
        creerDependance(taches, "form_agent_creation_concernee", "paye_creation_drh");
        creerDependance(taches, "badge_creation", "cantine_droits_creation_drh");
        
        // Dépendances pour les tâches DICI
        creerDependance(taches, "virtualia_creation", "organigramme_ajout_dici");
    }
    
    private void creerDependance(Map<String, UUID> taches, String sourceCode, String cibleCode) {
        UUID sourceTacheId = taches.get(sourceCode);
        UUID cibleTacheId = taches.get(cibleCode);
        
        if (sourceTacheId == null || cibleTacheId == null) {
            logger.warn("Impossible de créer la dépendance {} -> {} : certaines tâches sont manquantes", sourceCode, cibleCode);
            return;
        }
        
        // Charger toutes les dépendances avec leurs cibles dans une transaction
        List<TemplateDependanceEntity> allDependancesForCheck = templateDependanceJpaRepository.findAll();
        // Charger explicitement les collections cibles pour éviter LazyInitializationException
        allDependancesForCheck.forEach(d -> {
            // Accéder à la collection pour la charger dans la session
            d.getCibles().size();
        });
        
        boolean exists = allDependancesForCheck.stream()
            .anyMatch(d -> {
                if (d.getSourceTacheId() == null || !d.getSourceTacheId().equals(sourceTacheId)) {
                    return false;
                }
                List<TemplateDependanceCibleEntity> cibles = d.getCibles();
                if (cibles == null || cibles.isEmpty()) {
                    return false;
                }
                return cibles.stream().anyMatch(c -> c.getCibleTacheId().equals(cibleTacheId));
            });
        
        if (!exists) {
            Optional<TemplateDependanceEntity> dependanceExistante = allDependancesForCheck.stream()
                .filter(d -> d.getSourceTacheId() != null && d.getSourceTacheId().equals(sourceTacheId))
                .findFirst();
            
            if (dependanceExistante.isPresent()) {
                TemplateDependanceEntity dep = dependanceExistante.get();
                boolean cibleExiste = dep.getCibles().stream()
                    .anyMatch(c -> c.getCibleTacheId().equals(cibleTacheId));
                if (!cibleExiste) {
                    TemplateDependanceCibleEntity nouvelleCible = new TemplateDependanceCibleEntity(dep, cibleTacheId);
                    dep.getCibles().add(nouvelleCible);
                    templateDependanceJpaRepository.save(dep);
                }
            } else {
                TemplateDependanceEntity dependance = new TemplateDependanceEntity(
                    UUID.randomUUID(),
                    sourceTacheId
                );
                
                TemplateDependanceCibleEntity cible = new TemplateDependanceCibleEntity(dependance, cibleTacheId);
                dependance.getCibles().add(cible);
                
                templateDependanceJpaRepository.save(dependance);
            }
            
            templateTacheJpaRepository.findById(cibleTacheId).ifPresent(t -> {
                Optional<TemplateDependanceEntity> dep = allDependancesForCheck.stream()
                    .filter(d -> d.getSourceTacheId() != null && d.getSourceTacheId().equals(sourceTacheId))
                    .findFirst();
                if (dep.isPresent()) {
                    t.setDependanceId(dep.get().getId());
                    templateTacheJpaRepository.save(t);
                }
            });
        }
    }
    
    private String createFormulaireRHContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();

              Map<String, Object> imageAgent = new HashMap<>();
              imageAgent.put("id", "image_agent");
              imageAgent.put("type", "image");
              imageAgent.put("label", "Image");
              imageAgent.put("description", "Image");
              imageAgent.put("required", true);
              Map<String, Object> validationImage = new HashMap<>();
              validationImage.put("minSize", 100);
              validationImage.put("maxSize", 1000000);
              validationImage.put("mimeType", Arrays.asList("image/jpeg", "image/png"));
              imageAgent.put("validation", validationImage);
              champs.add(imageAgent);

            Map<String, Object> nomAgent = new HashMap<>();
            nomAgent.put("id", "nom_agent");
            nomAgent.put("type", "texte");
            nomAgent.put("label", "Nom");
            nomAgent.put("description", "Nom");
            nomAgent.put("required", true);
            Map<String, Object> validationNom = new HashMap<>();
            validationNom.put("minLength", 1);
            validationNom.put("maxLength", 100);
            nomAgent.put("validation", validationNom);
            champs.add(nomAgent);
            
            Map<String, Object> prenomAgent = new HashMap<>();
            prenomAgent.put("id", "prenom_agent");
            prenomAgent.put("type", "texte");
            prenomAgent.put("label", "Prénom");
            prenomAgent.put("description", "Prénom");
            prenomAgent.put("required", true);
            Map<String, Object> validationPrenom = new HashMap<>();
            validationPrenom.put("minLength", 1);
            validationPrenom.put("maxLength", 50);
            prenomAgent.put("validation", validationPrenom);
            champs.add(prenomAgent);
            Map<String, Object> numeroBureau = new HashMap<>();
            numeroBureau.put("id", "numero_bureau");
            numeroBureau.put("type", "texte");
            numeroBureau.put("label", "Numéro de bureau");
            numeroBureau.put("description", "Numéro de bureau");
            numeroBureau.put("required", false);
            numeroBureau.put("validation", Map.of( "maxLength", 100));
            champs.add(numeroBureau);

            Map<String, Object> emailAgent = new HashMap<>();
            emailAgent.put("id", "email_agent");
            emailAgent.put("type", "email");
            emailAgent.put("label", "Email");
            emailAgent.put("description", "Email");
            emailAgent.put("required", true);
            Map<String, Object> validationEmail = new HashMap<>();
            validationEmail.put("minLength", 1);
            validationEmail.put("maxLength", 50);
            validationEmail.put("pattern", "^[a-zA-Z0-9._%+-]+@lecese\\.fr$");
            emailAgent.put("validation", validationEmail);
            champs.add(emailAgent);
           
            
            Map<String, Object> dateArrivee = new HashMap<>();
            dateArrivee.put("id", "date_arrivee");
            dateArrivee.put("type", "date");
            dateArrivee.put("label", "Date d'arrivée");
            dateArrivee.put("description", "Date d'arrivée");
            dateArrivee.put("required", true);
            Map<String, Object> validationDateArrivee = new HashMap<>();
            validationDateArrivee.put("minDate", "-1-week");
            validationDateArrivee.put("maxDate", "year");
            dateArrivee.put("validation", validationDateArrivee);
            champs.add(dateArrivee);
            
            Map<String, Object> dateDepart = new HashMap<>();
            dateDepart.put("id", "date_depart");
            dateDepart.put("type", "date");
            dateDepart.put("label", "Date de départ (optionnelle)");
            dateDepart.put("description", "Date de départ (optionnelle)");
            dateDepart.put("required", false);
            Map<String, Object> validationDateDepart = new HashMap<>();
            validationDateDepart.put("minDate", "now");
            dateDepart.put("validation", validationDateDepart);
            champs.add(dateDepart);
            
            Map<String, Object> direction = new HashMap<>();
            direction.put("id", "direction");
            direction.put("type", "select");
            direction.put("label", "Direction");
            direction.put("description", "Direction");
            direction.put("required", true);
            Map<String, Object> optionsDirection = new HashMap<>();
            optionsDirection.put("accesBaseDonnees", "direction");
            optionsDirection.put("champs", Arrays.asList("lib_direction", "code_direction"));
            direction.put("options", optionsDirection);
            champs.add(direction);
            
            Map<String, Object> role = new HashMap<>();
            role.put("id", "role");
            role.put("type", "select");
            role.put("label", "Rôle");
            role.put("description", "Rôle");
            role.put("required", true);
            Map<String, Object> optionsRole = new HashMap<>();
            optionsRole.put("accesBaseDonnees", "role");
            optionsRole.put("champs", Arrays.asList("lib_role", "code_role"));
            role.put("options", optionsRole);
            champs.add(role);
            
            List<Map<String, Object>> actions = new ArrayList<>();
            
            Map<String, Object> actionUpdateAgent = new HashMap<>();
            actionUpdateAgent.put("type", "UPDATE_AGENT");
            actionUpdateAgent.put("condition", null);
            Map<String, Object> paramsUpdateAgent = new HashMap<>();
            paramsUpdateAgent.put("nom", "${formulaire.nom_agent}");
            paramsUpdateAgent.put("prenom", "${formulaire.prenom_agent}");
            paramsUpdateAgent.put("email", "${formulaire.email_agent}");
            paramsUpdateAgent.put("role", "${formulaire.role}");
            paramsUpdateAgent.put("etatAgent", "${processus.typeProcessus}");
            actionUpdateAgent.put("params", paramsUpdateAgent);
            actions.add(actionUpdateAgent);
            
            Map<String, Object> actionUpdateAgentDirection = new HashMap<>();
            actionUpdateAgentDirection.put("type", "UPDATE_AGENT_DIRECTION");
            actionUpdateAgentDirection.put("condition", null);
            Map<String, Object> paramsUpdateAgentDirection = new HashMap<>();
            paramsUpdateAgentDirection.put("agentId", "${agent.id}");
            paramsUpdateAgentDirection.put("directionId", "${formulaire.direction}");
            paramsUpdateAgentDirection.put("dateArrivee", "${formulaire.date_arrivee}");
            paramsUpdateAgentDirection.put("dateDepart", "${formulaire.date_depart}");
            paramsUpdateAgentDirection.put("numeroBureau", "${formulaire.numero_bureau}");
            actionUpdateAgentDirection.put("params", paramsUpdateAgentDirection);
            actions.add(actionUpdateAgentDirection);
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            contenu.put("actions", actions);
            
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour le formulaire RH", e);
            return "{}";
        }
    }
    
    private String createFormulaireConcerneeContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            
            Map<String, Object> fonction = new HashMap<>();
            fonction.put("id", "fonction");
            fonction.put("type", "texte");
            fonction.put("label", "Fonction");
            fonction.put("description", "Fonction");
            fonction.put("required", true);
            Map<String, Object> validationFonction = new HashMap<>();
            validationFonction.put("minLength", 1);
            validationFonction.put("maxLength", 300);
            fonction.put("validation", validationFonction);
            champs.add(fonction);
            
            Map<String, Object> responsable = new HashMap<>();
            responsable.put("id", "Responsable");
            responsable.put("type", "select");
            responsable.put("label", "Responsable");
            responsable.put("description", "Responsable");
            responsable.put("required", false);
            Map<String, Object> optionsResponsable = new HashMap<>();
            optionsResponsable.put("accesBaseDonnees", "agent");
            optionsResponsable.put("champs", Arrays.asList("lib_agent", "code_agent"));
            responsable.put("options", optionsResponsable);
            champs.add(responsable);
            
            Map<String, Object> agentAcceuil = new HashMap<>();
            agentAcceuil.put("id", "agent_acceuil");
            agentAcceuil.put("type", "select");
            agentAcceuil.put("label", "Personne chargée de l'accueil");
            agentAcceuil.put("description", "Personne chargée de l'accueil");
            agentAcceuil.put("required", false);
            Map<String, Object> optionsAgentAcceuil = new HashMap<>();
            optionsAgentAcceuil.put("accesBaseDonnees", "agent");
            optionsAgentAcceuil.put("champs", Arrays.asList("lib_agent", "code_agent"));
            agentAcceuil.put("options", optionsAgentAcceuil);
            champs.add(agentAcceuil);
            
            Map<String, Object> application = new HashMap<>();
            application.put("id", "application");
            application.put("type", "liste");
            application.put("label", "Application");
            application.put("description", "Application");
            application.put("required", false);
            champs.add(application);
            
            Map<String, Object> diffusion = new HashMap<>();
            diffusion.put("id", "diffusion");
            diffusion.put("type", "liste");
            diffusion.put("label", "Liste de diffusion");
            diffusion.put("description", "Liste de diffusion");
            diffusion.put("required", false);
            champs.add(diffusion);
            
            Map<String, Object> materiel = new HashMap<>();
            materiel.put("id", "materiel");
            materiel.put("type", "liste");
            materiel.put("label", "Matériel");
            materiel.put("description", "Matériel");
            materiel.put("required", false);
            champs.add(materiel);
            
            List<Map<String, Object>> actions = new ArrayList<>();
            
            Map<String, Object> actionUpdateAgentAffectation = new HashMap<>();
            actionUpdateAgentAffectation.put("type", "UPDATE_AGENT_AFFECTATION");
            actionUpdateAgentAffectation.put("condition", null);
            Map<String, Object> paramsUpdateAgentAffectation = new HashMap<>();
            paramsUpdateAgentAffectation.put("agentId", "${agent.id}");
            paramsUpdateAgentAffectation.put("directionId", "${agentDirection.directionId}");
            paramsUpdateAgentAffectation.put("fonction", "${formulaire.fonction}");
            paramsUpdateAgentAffectation.put("agentAcceuilId", "${formulaire.agent_acceuil}");
            paramsUpdateAgentAffectation.put("agentResponsableId", "${formulaire.Responsable}");
            actionUpdateAgentAffectation.put("params", paramsUpdateAgentAffectation);
            actions.add(actionUpdateAgentAffectation);
            
            Map<String, Object> actionUpdateAgentMateriel = new HashMap<>();
            actionUpdateAgentMateriel.put("type", "UPDATE_AGENT_MATERIEL");
            actionUpdateAgentMateriel.put("condition", null);
            Map<String, Object> paramsUpdateAgentMateriel = new HashMap<>();
            paramsUpdateAgentMateriel.put("agentId", "${agent.id}");
            Map<String, Object> materielEtDroits = new HashMap<>();
            materielEtDroits.put("materiels", "${formulaire.materiel}");
            materielEtDroits.put("droits", "${formulaire.application}");
            paramsUpdateAgentMateriel.put("materielData", materielEtDroits);
            actionUpdateAgentMateriel.put("params", paramsUpdateAgentMateriel);
            actions.add(actionUpdateAgentMateriel);
            
            Map<String, Object> actionUpdateAgentDiffusion = new HashMap<>();
            actionUpdateAgentDiffusion.put("type", "UPDATE_AGENT_DIFFUSION");
            actionUpdateAgentDiffusion.put("condition", null);
            Map<String, Object> paramsUpdateAgentDiffusion = new HashMap<>();
            paramsUpdateAgentDiffusion.put("agentId", "${agent.id}");
            paramsUpdateAgentDiffusion.put("listeDiffusion", "${formulaire.diffusion}");
            actionUpdateAgentDiffusion.put("params", paramsUpdateAgentDiffusion);
            actions.add(actionUpdateAgentDiffusion);
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            contenu.put("actions", actions);
            
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour le formulaire concerné", e);
            return "{}";
        }
    }
    
    private String createAdresseMailContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            
            Map<String, Object> adresseMail = new HashMap<>();
            adresseMail.put("id", "adresse_mail");
            adresseMail.put("type", "value");
            adresseMail.put("libelle", "Adresse mail à créer");
            adresseMail.put("baseDonnees", "agent");
            adresseMail.put("champs", Arrays.asList("email"));
            champs.add(adresseMail);
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour adresse_mail_creation", e);
            return "{}";
        }
    }
    private String createNumeroBureauContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            Map<String, Object> numeroBureau = new HashMap<>();
            numeroBureau.put("id", "numero_bureau");
            numeroBureau.put("type", "value");
            numeroBureau.put("libelle", "Clé du bureau à donner, le bureau étant aménagé :");
            numeroBureau.put("baseDonnees", "agent");
            numeroBureau.put("champs", Arrays.asList("numeroBureau"));
            champs.add(numeroBureau);
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour numero_bureau_creation", e);
            return "{}";
        }
    }
    

    private String createDroitAgentContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            
            Map<String, Object> droitAgent = new HashMap<>();
            droitAgent.put("id", "droit_agent");
            droitAgent.put("type", "value");
            droitAgent.put("libelle", "Droits à donner");
            droitAgent.put("baseDonnees", "agentMaterielEtDroit");
            droitAgent.put("champs", Arrays.asList("droits"));
            champs.add(droitAgent);

            Map<String, Object> diffusionAgent = new HashMap<>();
            diffusionAgent.put("id", "diffusion_agent");
            diffusionAgent.put("type", "value");
            diffusionAgent.put("libelle", "Diffusion à donner");
            diffusionAgent.put("baseDonnees", "agentDiffusion");
            diffusionAgent.put("champs", Arrays.asList("listeDiffusion"));
            champs.add(diffusionAgent);
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour droit_agent_creation", e);
            return "{}";
        }
    }
    
    private String createMaterielAgentContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            
            Map<String, Object> materielAgent = new HashMap<>();
            materielAgent.put("id", "materiels");
            materielAgent.put("type", "value");
            materielAgent.put("libelle", "Matériel à donner");
            materielAgent.put("baseDonnees", "agentMaterielEtDroit");
            materielAgent.put("champs", Arrays.asList("materiels"));
            champs.add(materielAgent);
            
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour materiel_agent_creation", e);
            return "{}";
        }
    }
}
