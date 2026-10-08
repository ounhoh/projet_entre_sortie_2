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
 * Initialiseur pour le processus "sortie_agent".
 * 
 * Cette classe gère l'initialisation de toutes les données nécessaires pour le processus "sortie_agent" :
 * - Le template processus
 * - Les statuts
 * - Les groupes de tâches
 * - Les tâches
 * - Les dépendances
 */
@Component
public class SortieAgentDataInitializer extends ProcessusDataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(SortieAgentDataInitializer.class);
    
    private final StatutProcessusJpaRepository statutProcessusJpaRepository;
    private final TemplateProcessusJpaRepository templateProcessusJpaRepository;
    private final TemplateGroupeTacheJpaRepository templateGroupeTacheJpaRepository;
    private final TemplateTacheJpaRepository templateTacheJpaRepository;
    private final TemplateGroupeTacheAssociationJpaRepository templateGroupeTacheAssociationJpaRepository;
    private final TemplateDependanceJpaRepository templateDependanceJpaRepository;
    private final DirectionJpaRepository directionJpaRepository;
    private final ObjectMapper objectMapper;
    
    public SortieAgentDataInitializer(
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
        return "sortie_agent";
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
        Optional<TemplateProcessusEntity> existing = templateProcessusJpaRepository.findByCodeProcessus("sortie_agent_creation");
        
        if (existing.isPresent()) {
            logger.info("Le template processus 'sortie_agent' existe déjà.");
            return existing.get().getId();
        }
        
        logger.info("Création du template processus 'sortie_agent'...");
        
        TemplateProcessusEntity template = new TemplateProcessusEntity(
            UUID.randomUUID(),
            "sortie_agent_creation",
            "Sortie",
            "Processus de sortie",
            TypeProcessus.sortie,
            true
        );
        
        TemplateProcessusEntity saved = templateProcessusJpaRepository.save(template);
        logger.info("Template processus 'sortie_agent' créé avec l'ID: {}", saved.getId());
        
        return saved.getId();
    }
    
    private Map<String, UUID> initialiserStatutsProcessus(UUID templateProcessusId) {
        Map<String, UUID> statutsMap = new HashMap<>();
        
        // Vérifier si les statuts pour ce template existent déjà
        List<StatutProcessusEntity> statutsExistants = statutProcessusJpaRepository.findByTemplateProcessusId(templateProcessusId);
        if (!statutsExistants.isEmpty()) {
            logger.info("Les statuts pour le template 'sortie_agent' existent déjà, récupération des IDs...");
            Map<String, Integer> ordreMap = Map.of(
                "Initialisation/Formulaire des droits", 1,
                "Formulaire des accès", 2,
                "Retrait des droits", 3,
                "Processus cloturé", 4,
                "Archivé", 5
            );
            
            for (StatutProcessusEntity statut : statutsExistants) {
                Integer ordreAttendu = ordreMap.get(statut.getLibStatut());
                if (ordreAttendu != null && statut.getOrdre() == null) {
                    statut.setOrdre(ordreAttendu);
                    statutProcessusJpaRepository.save(statut);
                }
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
        logger.info("Création des statuts de processus pour le template 'sortie_agent'...");
        
        StatutProcessusEntity statut1 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_sortie_agent_1",
            "Initialisation/Formulaire des droits",
            "étape du formulaire de sortie d'un agent pour remplir ses informations personnelles",
            1,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut1);
        statutsMap.put("Initialisation/Formulaire des droits", statut1.getId());
        logger.info("Statut 'Initialisation/Formulaire des droits' créé (ordre: 1)");
        
        StatutProcessusEntity statut2 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_sortie_agent_2",
            "Formulaire des accès",
            "étape du formulaire de sortie d'un agent pour remplir ses accès",
            2,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut2);
        statutsMap.put("Formulaire des accès", statut2.getId());
        logger.info("Statut 'Formulaire des accès' créé (ordre: 2)");
        
        StatutProcessusEntity statut3 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_sortie_agent_3",
            "Retrait des droits",
            "étape de réalisation des taches de retrait des droits d'un agent",
            3,
            templateProcessusId
        );
        statutProcessusJpaRepository.save(statut3);
        statutsMap.put("Retrait des droits", statut3.getId());
        logger.info("Statut 'Retrait des droits' créé (ordre: 3)");
        
        StatutProcessusEntity statut4 = new StatutProcessusEntity(
            UUID.randomUUID(),
            "statut_sortie_agent_4",
            "Processus cloturé",
            "étape de fin de sortie d'un agent",
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
    
    /**
     * Initialise les groupes de tâches du template "sortie_agent".
     * Retourne une Map avec le code du groupe comme clé et l'UUID comme valeur.
     */
    private Map<String, UUID> initialiserGroupesTaches(UUID templateProcessusId, Map<String, UUID> directions, Map<String, UUID> statuts) {
        Map<String, UUID> groupesMap = new HashMap<>();
        
        UUID drhId = directions.get("DRH");
        UUID dsiunId = directions.get("DSIUN");
        UUID dappiId = directions.get("DAPPI");
        UUID dafId = directions.get("DAF");
        UUID diciId = directions.get("DICI");
        if (drhId == null || dsiunId == null || dappiId == null || dafId == null || diciId == null) {
            logger.error("Directions manquantes pour le processus de sortie (DRH/DSIUN/DAPPI/DAF/DICI)");
            throw new IllegalStateException("Directions manquantes pour l'initialisation du processus de sortie");
        }
        
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
        UUID statutInit = statuts.get("Initialisation/Formulaire des droits");
        UUID statutAcces = statuts.get("Formulaire des accès");
        UUID statutRetrait = statuts.get("Retrait des droits");
        if (statutInit == null || statutAcces == null || statutRetrait == null) {
            logger.error("Statuts manquants pour le processus de sortie");
            throw new IllegalStateException("Statuts manquants pour l'initialisation du processus de sortie");
        }
        
        // Groupe 1: form_agent_sortie_rh (DRH)
        groupesMap.put("form_agent_sortie_rh", creerOuMettreAJourGroupe(
            "form_agent_sortie_rh",
            "Formulaire de sortie",
            codeDirectionDRH,
            statutInit,
            drhId,
            templateProcessusId,
            false
        ));
        
        // Groupe 2: agent_form_sortie_concernee (direction concernée)
        groupesMap.put("agent_form_sortie_concernee", creerOuMettreAJourGroupe(
            "agent_form_sortie_concernee",
            "Groupe de tâches de sortie",
            codeDirectionDRH,
            statutAcces,
            drhId,
            templateProcessusId,
            true
        ));
        
        // Groupe 3: virtualia_organigramme_sortie (DRH)
        groupesMap.put("virtualia_organigramme_sortie", creerOuMettreAJourGroupe(
            "virtualia_organigramme_sortie",
            "Virtualia et Organigramme",
            codeDirectionDRH,
            statutRetrait,
            drhId,
            templateProcessusId,
            false
        ));
        
        // Groupe 4: requea_sortie (DAPPI)
        groupesMap.put("requea_sortie", creerOuMettreAJourGroupe(
            "requea_sortie",
            "Requea",
            codeDirectionDAPPI,
            statutRetrait,
            dappiId,
            templateProcessusId,
            false
        ));
        
        // Groupe 5: adresse_mail_sortie (DSIUN)
        groupesMap.put("adresse_mail_sortie", creerOuMettreAJourGroupe(
            "adresse_mail_sortie",
            "Droits et Adresse mail",
            codeDirectionDSIUN,
            statutRetrait,
            dsiunId,
            templateProcessusId,
            false
        ));
        
        // Groupe 6: materiel_agent_sortie (DSIUN)
        groupesMap.put("materiel_agent_sortie", creerOuMettreAJourGroupe(
            "materiel_agent_sortie",
            "Matériel",
            codeDirectionDSIUN,
            statutRetrait,
            dsiunId,
            templateProcessusId,
            false
        ));
        
        // Groupe 7: daf_materiel_sortie (DAF)
        groupesMap.put("daf_materiel_sortie", creerOuMettreAJourGroupe(
            "daf_materiel_sortie",
            "Traitement du matériel non rendu",
            codeDirectionDAF,
            statutRetrait,
            dafId,
            templateProcessusId,
            false
        ));

        // Groupe 8: drh_materiel_sortie (DRH)
        groupesMap.put("drh_materiel_sortie", creerOuMettreAJourGroupe(
            "drh_materiel_sortie",
            "Traitement du matériel non rendu",
            codeDirectionDRH,
            statutRetrait,
            drhId,
            templateProcessusId,
            false
        ));

        // Groupe 8dici: organigramme_sortie_dici (DICI)
        groupesMap.put("organigramme_sortie_dici", creerOuMettreAJourGroupe(
            "organigramme_sortie_dici",
            "Retirer de l'organigramme",
            codeDirectionDICI,
            statutRetrait,
            diciId,
            templateProcessusId,
            false
        ));

        // Groupe 9: numero_bureau_sortie (DAPPI)
        groupesMap.put("numero_bureau_sortie", creerOuMettreAJourGroupe(
            "numero_bureau_sortie",
            "Clé du bureau à récupérer",
            codeDirectionDAPPI,
            statutRetrait,
            dappiId,
            templateProcessusId,
            false
        ));
        return groupesMap;
    }

    private UUID creerOuMettreAJourGroupe(
        String code,
        String libelle,
        String codeDirection,
        UUID statutId,
        UUID directionId,
        UUID templateProcessusId,
        boolean isDirectionConcernee
    ) {
        Optional<TemplateGroupeTacheEntity> existing = templateGroupeTacheJpaRepository.findByCodeTemplate(code);
        if (existing.isEmpty()) {
            TemplateGroupeTacheEntity groupe = new TemplateGroupeTacheEntity(
                UUID.randomUUID(),
                code,
                codeDirection,
                libelle,
                statutId,
                directionId,
                templateProcessusId,
                isDirectionConcernee
            );
            templateGroupeTacheJpaRepository.save(groupe);
            logger.info("Groupe '{}' créé (direction: {})", code, codeDirection);
            return groupe.getId();
        }
        
        TemplateGroupeTacheEntity groupe = existing.get();
        boolean updated = false;
        if (groupe.getCodeDirection() == null || !groupe.getCodeDirection().equals(codeDirection)) {
            groupe.setCodeDirection(codeDirection);
            updated = true;
        }
        if (groupe.getStatutProcessusId() == null || !groupe.getStatutProcessusId().equals(statutId)) {
            groupe.setStatutProcessusId(statutId);
            updated = true;
        }
        if (groupe.getDirectionId() == null || !groupe.getDirectionId().equals(directionId)) {
            groupe.setDirectionId(directionId);
            updated = true;
        }
        if (groupe.getTemplateProcessusId() == null || !groupe.getTemplateProcessusId().equals(templateProcessusId)) {
            groupe.setTemplateProcessusId(templateProcessusId);
            updated = true;
        }
        if (groupe.isDirectionConcernee() != isDirectionConcernee) {
            groupe.setDirectionConcernee(isDirectionConcernee);
            updated = true;
        }
        if (updated) {
            templateGroupeTacheJpaRepository.save(groupe);
        }
        return groupe.getId();
    }
    
    /**
     * Initialise les tâches du template "sortie_agent" et leurs associations avec les groupes.
     * Retourne une Map avec le code de la tâche comme clé et l'UUID comme valeur.
     */
    private Map<String, UUID> initialiserTaches(Map<String, UUID> groupes) {
        Map<String, UUID> tachesMap = new HashMap<>();
        
        // Tâche formulaire RH
        UUID tacheFormRhId = creerOuMettreAJourTache(
            "form_agent_sortie_rh",
            "Formulaire de sortie",
            "Tâche de sortie",
            TacheType.formulaire,
            0,
            createFormulaireSortieRHContenu()
        );
        tachesMap.put("form_agent_sortie_rh", tacheFormRhId);
        associerTacheAuGroupe(groupes.get("form_agent_sortie_rh"), tacheFormRhId, 1);
        
        // Tâche formulaire direction concernée
        UUID tacheFormConcerneeId = creerOuMettreAJourTache(
            "form_agent_sortie_concernee",
            "Formulaire de sortie pour la direction concernee",
            "Tâche de sortie pour la direction concernée",
            TacheType.formulaire,
            0,
            createFormulaireSortieConcerneeContenu()
        );
        tachesMap.put("form_agent_sortie_concernee", tacheFormConcerneeId);
        associerTacheAuGroupe(groupes.get("agent_form_sortie_concernee"), tacheFormConcerneeId, 1);
        
        // Tâches virtualia / organigramme
        UUID tacheVirtualiaId = creerOuMettreAJourTache(
            "virtualia_sortie",
            "Clôturer le compte virtualia",
            "Clôturer le compte virtualia",
            TacheType.tache,
            7,
            null
        );
        tachesMap.put("virtualia_sortie", tacheVirtualiaId);
        associerTacheAuGroupe(groupes.get("virtualia_organigramme_sortie"), tacheVirtualiaId, 1);
        
        UUID tacheOrganigrammeId = creerOuMettreAJourTache(
            "organigramme_sortie",
            "Retirer de l'organigramme",
            "Retirer de l'organigramme",
            TacheType.tache,
            7,
            null
        );
        tachesMap.put("organigramme_sortie", tacheOrganigrammeId);
        associerTacheAuGroupe(groupes.get("virtualia_organigramme_sortie"), tacheOrganigrammeId, 2);
        
        // Tâche organigramme_sortie_dici (pour DICI)
        UUID tacheOrganigrammeDiciId = creerOuMettreAJourTache(
            "organigramme_sortie_dici",
            "Retirer de l'organigramme",
            "Retirer de l'organigramme",
            TacheType.tache,
            7,
            null
        );
        tachesMap.put("organigramme_sortie_dici", tacheOrganigrammeDiciId);
        associerTacheAuGroupe(groupes.get("organigramme_sortie_dici"), tacheOrganigrammeDiciId, 1);
        
        // Tâche requea
        UUID tacheRequeaId = creerOuMettreAJourTache(
            "requea_sortie",
            "Suppression du compte Requea",
            "Suppression du compte Requea",
            TacheType.tache,
            7,
            null
        );
        tachesMap.put("requea_sortie", tacheRequeaId);
        associerTacheAuGroupe(groupes.get("requea_sortie"), tacheRequeaId, 1);
        
        // Tâche droit agent sortie (value)
        UUID tacheDroitId = creerOuMettreAJourTache(
            "droit_agent_sortie",
            "Retrait des droits",
            "Retrait des droits",
            TacheType.tache,
            7,
            createDroitAgentSortieContenu()
        );
        tachesMap.put("droit_agent_sortie", tacheDroitId);
        associerTacheAuGroupe(groupes.get("adresse_mail_sortie"), tacheDroitId, 1);
        
        // Tâche adresse mail sortie (value)
        UUID tacheAdresseId = creerOuMettreAJourTache(
            "adresse_mail_sortie",
            "Suppression de l'adresse mail",
            "Suppression de l'adresse mail",
            TacheType.tache,
            84,
            createAdresseMailSortieContenu()
        );
        tachesMap.put("adresse_mail_sortie", tacheAdresseId);
        associerTacheAuGroupe(groupes.get("adresse_mail_sortie"), tacheAdresseId, 2);
        
        // Tâche matériel agent sortie (etat_changement)
        UUID tacheMaterielId = creerOuMettreAJourTache(
            "materiel_agent_sortie",
            "Matériel",
            "Retourner le matériel",
            TacheType.tache,
            7,
            createMaterielAgentSortieContenu()
        );
        tachesMap.put("materiel_agent_sortie", tacheMaterielId);
        associerTacheAuGroupe(groupes.get("materiel_agent_sortie"), tacheMaterielId, 1);
        
        // Tâche DAF matériel agent sortie (etat_changement)
        UUID tacheDAFMaterielId = creerOuMettreAJourTache(
            "daf_materiel_sortie",
            "Traitement du matériel non rendu",
            "Traitement du matériel non rendu",
            TacheType.tache,
            7,
            createDAFMaterielSortieContenu()
        );
        tachesMap.put("daf_materiel_sortie", tacheDAFMaterielId);
        associerTacheAuGroupe(groupes.get("daf_materiel_sortie"), tacheDAFMaterielId, 1);

        // Tâche DRH matériel agent sortie (etat_changement)
        UUID tachedrhMaterielId = creerOuMettreAJourTache(
            "daf_materiel_sortie_drh",
            "Traitement du matériel non rendu",
            "Traitement du matériel non rendu",
            TacheType.tache,
            7,
            createDAFMaterielSortieContenu()
        );
        tachesMap.put("daf_materiel_sortie_drh", tachedrhMaterielId);
        associerTacheAuGroupe(groupes.get("drh_materiel_sortie"), tachedrhMaterielId, 1);

        // Tâche numero_bureau_sortie (value)
        UUID tacheNumeroBureauId = creerOuMettreAJourTache(
            "numero_bureau_sortie",
            "Clé du bureau à récupérer",
            "Clé du bureau à récupérer",
            TacheType.tache,
            7,
            createNumeroBureauSortieContenu()
        );
        tachesMap.put("numero_bureau_sortie", tacheNumeroBureauId);
        associerTacheAuGroupe(groupes.get("numero_bureau_sortie"), tacheNumeroBureauId, 1);

        return tachesMap;
    }

    private UUID creerOuMettreAJourTache(
        String code,
        String libelle,
        String description,
        TacheType type,
        int delaiJour,
        String contenu
    ) {
        Optional<TemplateTacheEntity> existing = templateTacheJpaRepository.findByCode(code);
        if (existing.isEmpty()) {
            TemplateTacheEntity tache = new TemplateTacheEntity(
                UUID.randomUUID(),
                code,
                libelle,
                description,
                type,
                delaiJour,
                contenu,
                null
            );
            templateTacheJpaRepository.save(tache);
            return tache.getId();
        }
        
        TemplateTacheEntity tache = existing.get();
        boolean updated = false;
        if (tache.getType() != type) {
            tache.setType(type);
            updated = true;
        }
        if (tache.getDelaiJour() != delaiJour) {
            tache.setDelaiJour(delaiJour);
            updated = true;
        }
        if (contenu != null && (tache.getContenu() == null || tache.getContenu().trim().isEmpty()
            || !contenu.equals(tache.getContenu()))) {
            tache.setContenu(contenu);
            updated = true;
        }
        if (updated) {
            templateTacheJpaRepository.save(tache);
        }
        return tache.getId();
    }

    private void associerTacheAuGroupe(UUID groupeId, UUID tacheId, int ordre) {
        if (groupeId == null || tacheId == null) {
            return;
        }
        boolean exists = templateGroupeTacheAssociationJpaRepository.findByTemplateGroupeId(groupeId).stream()
            .anyMatch(a -> a.getTemplateTacheId().equals(tacheId));
        if (!exists) {
            templateGroupeTacheAssociationJpaRepository.save(
                new TemplateGroupeTacheAssociationEntity(groupeId, tacheId, ordre)
            );
        }
    }
    
    /**
     * Initialise les dépendances entre tâches pour le processus "sortie_agent".
     */
    private void initialiserDependances(Map<String, UUID> taches, Map<String, UUID> groupes) {
        creerDependance(taches, "form_agent_sortie_rh", "form_agent_sortie_concernee");
        creerDependance(taches, "form_agent_sortie_concernee", "virtualia_sortie");
        creerDependance(taches, "form_agent_sortie_concernee", "materiel_agent_sortie");
        creerDependance(taches, "form_agent_sortie_concernee", "droit_agent_sortie");
        creerDependance(taches, "form_agent_sortie_concernee", "requea_sortie");
        creerDependance(taches, "virtualia_sortie", "organigramme_sortie");
        creerDependance(taches, "virtualia_sortie", "organigramme_sortie_dici");
        creerDependance(taches, "droit_agent_sortie", "adresse_mail_sortie");
        creerDependance(taches, "materiel_agent_sortie", "daf_materiel_sortie");
        creerDependance(taches, "materiel_agent_sortie", "daf_materiel_sortie_drh");
        creerDependance(taches, "form_agent_sortie_concernee", "numero_bureau_sortie");
    }

    private void creerDependance(Map<String, UUID> taches, String sourceCode, String cibleCode) {
        UUID sourceTacheId = taches.get(sourceCode);
        UUID cibleTacheId = taches.get(cibleCode);
        if (sourceTacheId == null || cibleTacheId == null) {
            logger.warn("Impossible de créer la dépendance {} -> {} : tâche manquante", sourceCode, cibleCode);
            return;
        }
        
        List<TemplateDependanceEntity> allDependances = templateDependanceJpaRepository.findAll();
        allDependances.forEach(d -> d.getCibles().size());
        
        boolean exists = allDependances.stream().anyMatch(d ->
            d.getSourceTacheId() != null &&
            d.getSourceTacheId().equals(sourceTacheId) &&
            d.getCibles().stream().anyMatch(c -> c.getCibleTacheId().equals(cibleTacheId))
        );
        if (exists) {
            return;
        }
        
        Optional<TemplateDependanceEntity> dependanceExistante = allDependances.stream()
            .filter(d -> d.getSourceTacheId() != null && d.getSourceTacheId().equals(sourceTacheId))
            .findFirst();
        
        if (dependanceExistante.isPresent()) {
            TemplateDependanceEntity dep = dependanceExistante.get();
            TemplateDependanceCibleEntity cible = new TemplateDependanceCibleEntity(dep, cibleTacheId);
            dep.getCibles().add(cible);
            templateDependanceJpaRepository.save(dep);
        } else {
            TemplateDependanceEntity dependance = new TemplateDependanceEntity(UUID.randomUUID(), sourceTacheId);
            TemplateDependanceCibleEntity cible = new TemplateDependanceCibleEntity(dependance, cibleTacheId);
            dependance.getCibles().add(cible);
            templateDependanceJpaRepository.save(dependance);
        }
        
        templateTacheJpaRepository.findById(cibleTacheId).ifPresent(t -> {
            Optional<TemplateDependanceEntity> dep = templateDependanceJpaRepository.findAll().stream()
                .filter(d -> d.getSourceTacheId() != null && d.getSourceTacheId().equals(sourceTacheId))
                .findFirst();
            dep.ifPresent(d -> {
                t.setDependanceId(d.getId());
                templateTacheJpaRepository.save(t);
            });
        });
    }

    private String createFormulaireSortieRHContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            
            Map<String, Object> imageAgent = new HashMap<>();
            imageAgent.put("id", "image_agent");
            imageAgent.put("type", "image");
            imageAgent.put("label", "Image");
            imageAgent.put("description", "Image");
            imageAgent.put("required", false);
            imageAgent.put("default", false);
            imageAgent.put("validation", Map.of(
                "minSize", 100,
                "maxSize", 1000000,
                "mimeType", Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
            ));
            champs.add(imageAgent);
            
            Map<String, Object> nomAgent = new HashMap<>();
            nomAgent.put("id", "nom_agent");
            nomAgent.put("type", "texte");
            nomAgent.put("label", "Nom");
            nomAgent.put("description", "Nom");
            nomAgent.put("required", true);
            nomAgent.put("validation", Map.of("minLength", 1, "maxLength", 100));
            nomAgent.put("alreadyExist", Map.of(
                "type", "agent",
                "champs", Arrays.asList("nom"),
                "baseDonnees", "agent"
            ));
            champs.add(nomAgent);
            
            Map<String, Object> prenomAgent = new HashMap<>();
            prenomAgent.put("id", "prenom_agent");
            prenomAgent.put("type", "texte");
            prenomAgent.put("label", "Prénom");
            prenomAgent.put("description", "Prénom");
            prenomAgent.put("required", true);
            prenomAgent.put("validation", Map.of("minLength", 1, "maxLength", 100));
            prenomAgent.put("alreadyExist", Map.of(
                "type", "agent",
                "champs", Arrays.asList("prenom"),
                "baseDonnees", "agent"
            ));
            champs.add(prenomAgent);
            
            Map<String, Object> emailAgent = new HashMap<>();
            emailAgent.put("id", "email_agent");
            emailAgent.put("type", "email");
            emailAgent.put("label", "Email");
            emailAgent.put("description", "Email");
            emailAgent.put("required", true);
            emailAgent.put("validation", Map.of(
                "minLength", 1,
                "maxLength", 100,
                "pattern", "^[a-zA-Z0-9._%+-]+@lecese\\.fr$"
            ));
            emailAgent.put("alreadyExist", Map.of(
                "type", "agent",
                "champs", Arrays.asList("email"),
                "baseDonnees", "agent"
            ));
            champs.add(emailAgent);
            Map<String, Object> numeroBureau = new HashMap<>();
            numeroBureau.put("id", "numero_bureau");
            numeroBureau.put("type", "texte");
            numeroBureau.put("label", "Numéro de bureau");
            numeroBureau.put("description", "Numéro de bureau");
            numeroBureau.put("validation", Map.of( "maxLength", 100));
            numeroBureau.put("required", false);
            champs.add(numeroBureau);

            Map<String, Object> dateArrivee = new HashMap<>();
            dateArrivee.put("id", "date_arrivee");
            dateArrivee.put("type", "date");
            dateArrivee.put("label", "Date d'arrivée");
            dateArrivee.put("description", "Date d'arrivée");
            dateArrivee.put("required", true);
            dateArrivee.put("default", "now");
            dateArrivee.put("validation", Map.of("maxDate", "now"));
            dateArrivee.put("alreadyExist", Map.of(
                "type", "agent",
                "champs", Arrays.asList("date_arrivee"),
                "baseDonnees", "agent"
            ));
            champs.add(dateArrivee);
            
            Map<String, Object> dateDepart = new HashMap<>();
            dateDepart.put("id", "date_depart");
            dateDepart.put("type", "date");
            dateDepart.put("label", "Date de départ");
            dateDepart.put("description", "Date de départ");
            dateDepart.put("required", true);
            dateDepart.put("default", null);
            dateDepart.put("validation", Map.of("minDate", "now"));
            dateDepart.put("alreadyExist", Map.of(
                "type", "agent",
                "champs", Arrays.asList("date_depart"),
                "baseDonnees", "agent"
            ));
            champs.add(dateDepart);
            
            Map<String, Object> direction = new HashMap<>();
            direction.put("id", "direction");
            direction.put("type", "select");
            direction.put("label", "Direction");
            direction.put("description", "Direction");
            direction.put("required", true);
            direction.put("options", Map.of(
                "accesBaseDonnees", "direction",
                "champs", Arrays.asList("lib_direction", "code_direction")
            ));
            direction.put("alreadyExist", Map.of(
                "type", "direction",
                "champs", Arrays.asList("direction"),
                "baseDonnees", "agent"
            ));
            champs.add(direction);
            
            Map<String, Object> role = new HashMap<>();
            role.put("id", "role");
            role.put("type", "select");
            role.put("label", "Rôle");
            role.put("description", "Rôle");
            role.put("required", true);
            role.put("options", Map.of(
                "accesBaseDonnees", "role",
                "champs", Arrays.asList("lib_role", "code_role")
            ));
            role.put("alreadyExist", Map.of(
                "type", "role",
                "champs", Arrays.asList("role"),
                "baseDonnees", "agent"
            ));
            champs.add(role);
            
            List<Map<String, Object>> actions = new ArrayList<>();
            Map<String, Object> actionUpdateAgent = new HashMap<>();
            actionUpdateAgent.put("type", "UPDATE_AGENT");
            actionUpdateAgent.put("condition", null);
            actionUpdateAgent.put("params", Map.of(
                "nom", "${formulaire.nom_agent}",
                "prenom", "${formulaire.prenom_agent}",
                "email", "${formulaire.email_agent}",
                "role", "${formulaire.role}",
                "etatAgent", "${processus.typeProcessus}"
            ));
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
            logger.error("Erreur lors de la création du contenu JSON pour le formulaire sortie RH", e);
            return "{}";
        }
    }

    private String createFormulaireSortieConcerneeContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            Map<String, Object> fonction = new HashMap<>();
            fonction.put("id", "fonction");
            fonction.put("type", "texte");
            fonction.put("label", "Fonction");
            fonction.put("description", "Fonction");
            fonction.put("required", true);
            fonction.put("validation", Map.of("minLength", 1, "maxLength", 300));
            fonction.put("alreadyExist", Map.of(
                "type", "affectation",
                "champs", Arrays.asList("fonction"),
                "baseDonnees", "affectation"
            ));
            champs.add(fonction);
            
            Map<String, Object> responsable = new HashMap<>();
            responsable.put("id", "Responsable");
            responsable.put("type", "select");
            responsable.put("label", "Responsable");
            responsable.put("description", "Responsable");
            responsable.put("required", false);
            responsable.put("options", Map.of(
                "accesBaseDonnees", "agent",
                "champs", Arrays.asList("lib_agent", "code_agent")
            ));
            responsable.put("alreadyExist", Map.of(
                "type", "affectation",
                "champs", Arrays.asList("agentResponsableId"),
                "baseDonnees", "affectation"
            ));
            champs.add(responsable);
            
            Map<String, Object> agentAcceuil = new HashMap<>();
            agentAcceuil.put("id", "agent_acceuil");
            agentAcceuil.put("type", "select");
            agentAcceuil.put("label", "Personne chargée de l'accueil");
            agentAcceuil.put("description", "Personne chargée de l'accueil");
            agentAcceuil.put("required", false);
            agentAcceuil.put("options", Map.of(
                "accesBaseDonnees", "agent",
                "champs", Arrays.asList("lib_agent", "code_agent")
            ));
            agentAcceuil.put("alreadyExist", Map.of(
                "type", "affectation",
                "champs", Arrays.asList("agentAcceuilId"),
                "baseDonnees", "affectation"
            ));
            champs.add(agentAcceuil);
            
            Map<String, Object> application = new HashMap<>();
            application.put("id", "application");
            application.put("type", "liste");
            application.put("label", "Application");
            application.put("description", "Application");
            application.put("required", false);
            application.put("validation", Map.of("minLength", 1, "maxLength", 300));
            application.put("alreadyExist", Map.of(
                "type", "agentMaterielEtDroit",
                "champs", Arrays.asList("droits"),
                "baseDonnees", "agentMaterielEtDroit"
            ));
            champs.add(application);
            
            Map<String, Object> diffusion = new HashMap<>();
            diffusion.put("id", "diffusion");
            diffusion.put("type", "liste");
            diffusion.put("label", "Liste de diffusion");
            diffusion.put("description", "Liste de diffusion");
            diffusion.put("required", false);
            diffusion.put("alreadyExist", Map.of(
                "type", "agentDiffusion",
                "champs", Arrays.asList("listeDiffusion"),
                "baseDonnees", "agentDiffusion"
            ));
            champs.add(diffusion);
            
            Map<String, Object> materiel = new HashMap<>();
            materiel.put("id", "materiel");
            materiel.put("type", "liste");
            materiel.put("label", "Matériel");
            materiel.put("description", "Matériel");
            materiel.put("required", false);
            materiel.put("alreadyExist", Map.of(
                "type", "agentMaterielEtDroit",
                "champs", Arrays.asList("materiels"),
                "baseDonnees", "agentMaterielEtDroit"
            ));
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
            
            Map<String, Object> actionUpdateMateriel = new HashMap<>();
            actionUpdateMateriel.put("type", "UPDATE_AGENT_MATERIEL");
            actionUpdateMateriel.put("condition", null);
            actionUpdateMateriel.put("params", Map.of(
                "agentId", "${agent.id}",
                "materielData", Map.of(
                    "materiels", "${formulaire.materiel}",
                    "droits", "${formulaire.application}"
                )
            ));
            actions.add(actionUpdateMateriel);
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            contenu.put("actions", actions);
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour le formulaire sortie concernée", e);
            return "{}";
        }
    }

    private String createDroitAgentSortieContenu() {
        try {
            Map<String, Object> champ = new HashMap<>();
            champ.put("id", "droit_agent");
            champ.put("type", "value");
            champ.put("libelle", "Droits à retirer");
            champ.put("baseDonnees", "agentMaterielEtDroit");
            champ.put("champs", Arrays.asList("droits"));
            
            Map<String, Object> champDiffusion = new HashMap<>();
            champDiffusion.put("id", "diffusion_agent");
            champDiffusion.put("type", "value");
            champDiffusion.put("libelle", "Liste de diffusion à retirer");
            champDiffusion.put("baseDonnees", "agentDiffusion");
            champDiffusion.put("champs", Arrays.asList("listeDiffusion"));
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", Arrays.asList(champ, champDiffusion));
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour droit_agent_sortie", e);
            return "{}";
        }
    }

    private String createAdresseMailSortieContenu() {
        try {
            Map<String, Object> champ = new HashMap<>();
            champ.put("id", "adresse_mail");
            champ.put("type", "value");
            champ.put("libelle", "Adresse mail à supprimer");
            champ.put("baseDonnees", "agent");
            champ.put("champs", Arrays.asList("email"));
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", Arrays.asList(champ));
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour adresse_mail_sortie", e);
            return "{}";
        }
    }
    private String createMaterielAgentSortieContenu() {
        try {
            Map<String, Object> champ = new HashMap<>();
            champ.put("id", "materiel");
            champ.put("type", "value");
            champ.put("libelle", "Matériel à rendre");
            champ.put("baseDonnees", "agentMaterielEtDroit");
            champ.put("champs", Arrays.asList("materiels"));
           
            Map<String, Object> materiel = new HashMap<>();
            materiel.put("id", "materiel_non_rendu");
            materiel.put("type", "liste");
            materiel.put("label", "Matériel non rendu");
            materiel.put("description", "Matériel non rendu");
            materiel.put("required", false);

            Map<String, Object> action = new HashMap<>();
            action.put("type", "UPDATE_AGENT_MATERIEL");
            action.put("condition", null);
            action.put("params", Map.of(
                "agentId", "${agent.id}",
                "materielData", Map.of(
                    "materiels", "${formulaire.materiel_non_rendu}"
                )
            ));
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", Arrays.asList(champ, materiel));
            contenu.put("actions", Arrays.asList(action));
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour materiel_agent_sortie", e);
            return "{}";
        }
    }
    private String createNumeroBureauSortieContenu() {
        try {
            List<Map<String, Object>> champs = new ArrayList<>();
            Map<String, Object> numeroBureau = new HashMap<>();
            numeroBureau.put("id", "numero_bureau");
            numeroBureau.put("type", "value");
            numeroBureau.put("libelle", "Clé du bureau à récupérer :");
            numeroBureau.put("baseDonnees", "agent");
            numeroBureau.put("champs", Arrays.asList("numeroBureau"));
            champs.add(numeroBureau);
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", champs);
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour numero_bureau_sortie", e);
            return "{}";
        }
    }
    private String createDAFMaterielSortieContenu() {

        try {
            Map<String, Object> champ = new HashMap<>();
            champ.put("id", "materiel");
            champ.put("type", "value");
            champ.put("libelle", "Matériel non rendu");
            champ.put("baseDonnees", "agentMaterielEtDroit");
            champ.put("champs", Arrays.asList("materiels"));
            
            
            Map<String, Object> contenu = new HashMap<>();
            contenu.put("champs", Arrays.asList(champ));
            return objectMapper.writeValueAsString(contenu);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du contenu JSON pour materiel_agent_sortie", e);
            return "{}";
        }
    }
}
