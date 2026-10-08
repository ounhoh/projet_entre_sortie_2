package com.cese.process_entree_sortie.infrastructure.config;

import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.*;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test") // Ne pas exécuter lors des tests
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleJpaRepository roleJpaRepository;
    private final DirectionJpaRepository directionJpaRepository;
    private final AgentPersonnelJpaRepository agentPersonnelJpaRepository;
    private final AgentDirectionJpaRepository agentDirectionJpaRepository;
    private final EntreeAgentDataInitializer entreeAgentDataInitializer;
    private final SortieAgentDataInitializer sortieAgentDataInitializer;

    public DataInitializer(
            RoleJpaRepository roleJpaRepository,
            DirectionJpaRepository directionJpaRepository,
            AgentPersonnelJpaRepository agentPersonnelJpaRepository,
            AgentDirectionJpaRepository agentDirectionJpaRepository,
            EntreeAgentDataInitializer entreeAgentDataInitializer,
            SortieAgentDataInitializer sortieAgentDataInitializer) {
        this.roleJpaRepository = roleJpaRepository;
        this.directionJpaRepository = directionJpaRepository;
        this.agentPersonnelJpaRepository = agentPersonnelJpaRepository;
        this.agentDirectionJpaRepository = agentDirectionJpaRepository;
        this.entreeAgentDataInitializer = entreeAgentDataInitializer;
        this.sortieAgentDataInitializer = sortieAgentDataInitializer;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initialisation des données...");

        initialiserRoles();
        Map<String, UUID> directions = initialiserDirections();

        // Utilisation des initializers spécifiques aux processus
        entreeAgentDataInitializer.initialize(directions);
        sortieAgentDataInitializer.initialize(directions);

        initialiserAgents(directions);

        logger.info("Initialisation des données terminée.");
    }

    /**
     * Initialise les rôles dans la base de données.
     */
    private void initialiserRoles() {
        logger.info("Initialisation des rôles...");

        // Rôle AGENT
        Optional<RoleEntity> roleAgent = roleJpaRepository.findByCode("AGENT");
        if (roleAgent.isEmpty()) {
            RoleEntity agent = new RoleEntity(UUID.randomUUID(), "AGENT", "Agent", 1);
            roleJpaRepository.save(agent);
            logger.info("Rôle 'AGENT' créé");
        }

        // Rôle MANAGER
        Optional<RoleEntity> roleManager = roleJpaRepository.findByCode("MANAGER");
        if (roleManager.isEmpty()) {
            RoleEntity manager = new RoleEntity(UUID.randomUUID(), "MANAGER", "Manager", 2);
            roleJpaRepository.save(manager);
            logger.info("Rôle 'MANAGER' créé");
        }

        // Rôle ADMIN
        Optional<RoleEntity> roleAdmin = roleJpaRepository.findByCode("ADMIN");
        if (roleAdmin.isEmpty()) {
            RoleEntity admin = new RoleEntity(UUID.randomUUID(), "ADMIN", "Admin", 3);
            roleJpaRepository.save(admin);
            logger.info("Rôle 'ADMIN' créé");
        }

        // Rôle PRESTATAIRE
        Optional<RoleEntity> rolePrestataire = roleJpaRepository.findByCode("PRESTATAIRE");
        if (rolePrestataire.isEmpty()) {
            RoleEntity prestataire = new RoleEntity(UUID.randomUUID(), "PRESTATAIRE", "Prestataire", 4);
            roleJpaRepository.save(prestataire);
            logger.info("Rôle 'PRESTATAIRE' créé");
        }

        // Rôle CONSEILLER
        Optional<RoleEntity> roleConseiller = roleJpaRepository.findByCode("CONSEILLER");
        if (roleConseiller.isEmpty()) {
            RoleEntity conseiller = new RoleEntity(UUID.randomUUID(), "CONSEILLER", "Conseiller", 5);
            roleJpaRepository.save(conseiller);
            logger.info("Rôle 'CONSEILLER' créé");
        }
    }

    /**
     * Initialise les directions dans la base de données.
     * Retourne une Map avec le code de la direction comme clé et l'UUID comme valeur.
     */
    private Map<String, UUID> initialiserDirections() {
        logger.info("Initialisation des directions...");
        Map<String, UUID> directionsMap = new HashMap<>();

        // Direction DRH
        Optional<DirectionEntity> drh = directionJpaRepository.findByCodeDirection("direction_rh");
        if (drh.isEmpty()) {
            DirectionEntity directionDrh = new DirectionEntity(UUID.randomUUID(),"direction_rh","DRH","Direction des Ressources Humaines");
            directionJpaRepository.save(directionDrh);
            directionsMap.put("DRH", directionDrh.getId());
            logger.info("Direction 'DRH' créée");
        } else {
            directionsMap.put("DRH", drh.get().getId());
        }

        // Direction DSIUN
        Optional<DirectionEntity> dsiun = directionJpaRepository.findByCodeDirection("direction_siun");
        if (dsiun.isEmpty()) {
            DirectionEntity directionDsiun = new DirectionEntity(UUID.randomUUID(),"direction_siun","DSIUN","Direction des Systèmes d'Information et des Usages Numériques");
            directionJpaRepository.save(directionDsiun);
            directionsMap.put("DSIUN", directionDsiun.getId());
            logger.info("Direction 'DSIUN' créée");
        } else {
            directionsMap.put("DSIUN", dsiun.get().getId());
        }

        // Direction DAPPI
        Optional<DirectionEntity> dappi = directionJpaRepository.findByCodeDirection("direction_appi");
        if (dappi.isEmpty()) {
            DirectionEntity directionDappi = new DirectionEntity(UUID.randomUUID(),"direction_appi","DAPPI","Direction de l'acceuil des publics et du patrimoine immobilier");
            directionJpaRepository.save(directionDappi);
            directionsMap.put("DAPPI", directionDappi.getId());
            logger.info("Direction 'DAPPI' créée");
        } else {
            directionsMap.put("DAPPI", dappi.get().getId());
        }

        // Direction DAF
        Optional<DirectionEntity> daf = directionJpaRepository.findByCodeDirection("direction_af");
        if (daf.isEmpty()) {
            DirectionEntity directionDaf = new DirectionEntity(UUID.randomUUID(),"direction_af","DAF","Direction Administrative et Financière");
            directionJpaRepository.save(directionDaf);
            directionsMap.put("DAF", directionDaf.getId());
            logger.info("Direction 'DAF' créée");
        } else {
            directionsMap.put("DAF", daf.get().getId());
        }

        // Direction DREI
        Optional<DirectionEntity> drei = directionJpaRepository.findByCodeDirection("direction_rei");
        if (drei.isEmpty()) {
            DirectionEntity directionDrei = new DirectionEntity(UUID.randomUUID(),"direction_rei","DREI","Direction des Relations Européennes et Internationales");
            directionJpaRepository.save(directionDrei);
            directionsMap.put("DREI", directionDrei.getId());
            logger.info("Direction 'DREI' créée");
        } else {
            directionsMap.put("DREI", drei.get().getId());
        }

        // Direction DICI
        Optional<DirectionEntity> dici = directionJpaRepository.findByCodeDirection("direction_ici");
        if (dici.isEmpty()) {
            DirectionEntity directionDici = new DirectionEntity(UUID.randomUUID(),"direction_ici","DICI","Direction de l'Innovation et de la communication");
            directionJpaRepository.save(directionDici);
            directionsMap.put("DICI", directionDici.getId());
            logger.info("Direction 'DICI' créée");
        } else {
            directionsMap.put("DICI", dici.get().getId());
        }

        // Direction DICOM
        Optional<DirectionEntity> dicom = directionJpaRepository.findByCodeDirection("direction_icom");
        if (dicom.isEmpty()) {
            DirectionEntity directionDicom = new DirectionEntity(UUID.randomUUID(),"direction_icom","DICOM","Direction de la communication");
            directionJpaRepository.save(directionDicom);
            directionsMap.put("DICOM", directionDicom.getId());
            logger.info("Direction 'DICOM' créée");
        } else {
            directionsMap.put("DICOM", dicom.get().getId());
        }

        // Direction DPC
        Optional<DirectionEntity> dpc = directionJpaRepository.findByCodeDirection("direction_pc");
        if (dpc.isEmpty()) {
            DirectionEntity directionDpc = new DirectionEntity(UUID.randomUUID(),"direction_pc","DPC","Direction de la Participation Citoyenne");
            directionJpaRepository.save(directionDpc);
            directionsMap.put("DPC", directionDpc.getId());
            logger.info("Direction 'DPC' créée");
        } else {
            directionsMap.put("DPC", dpc.get().getId());
        }


        // Direction DSC
        Optional<DirectionEntity> dsc = directionJpaRepository.findByCodeDirection("direction_sc");
        if (dsc.isEmpty()) {
            DirectionEntity directionDsc = new DirectionEntity(UUID.randomUUID(),"direction_sc","DSC","Direction des Services Consultatifs");
            directionJpaRepository.save(directionDsc);
            directionsMap.put("DSC", directionDsc.getId());
            logger.info("Direction 'DSC' créée");
        } else {
            directionsMap.put("DSC", dsc.get().getId());
        }

        // Direction SG
        Optional<DirectionEntity> sg = directionJpaRepository.findByCodeDirection("direction_sg");
        if (sg.isEmpty()) {
            DirectionEntity directionSg = new DirectionEntity(UUID.randomUUID(),"direction_sg","SG","Secrétariat Général");
            directionJpaRepository.save(directionSg);
            directionsMap.put("SG", directionSg.getId());
            logger.info("Direction 'SG' créée");
        } else {
            directionsMap.put("SG", sg.get().getId());
        }

        // Direction CAB
        Optional<DirectionEntity> cab = directionJpaRepository.findByCodeDirection("direction_cab");
        if (cab.isEmpty()) {
            DirectionEntity directionCab = new DirectionEntity(UUID.randomUUID(),"direction_cab","CAB","Cabinet");
            directionJpaRepository.save(directionCab);
            directionsMap.put("CAB", directionCab.getId());
            logger.info("Direction 'CAB' créée");
        } else {
            directionsMap.put("CAB", cab.get().getId());
        }

        return directionsMap;
    }

    /**
     * Initialise les agents de test dans la base de données.
     */
    private void initialiserAgents(Map<String, UUID> directions) {
        logger.info("Initialisation des agents...");

        // Récupérer le rôle Admin
        RoleEntity roleAdmin = roleJpaRepository.findByCode("ADMIN")
            .orElseThrow(() -> new IllegalStateException("Rôle ADMIN non trouvé. Veuillez initialiser les rôles d'abord."));

        // Récupérer les directions nécessaires
        UUID drhId = directions.get("DRH");
        UUID dappiId = directions.get("DAPPI");
        UUID dsiunId = directions.get("DSIUN");
        UUID dafId = directions.get("DAF");
        UUID diciId = directions.get("DICI");

        if (drhId == null || dappiId == null || dsiunId == null || dafId == null || diciId == null) {
            logger.error("Directions nécessaires non trouvées ! Impossible de créer les agents.");
            return;
        }

        // Agent 1: John Doe
        Optional<AgentPersonnelEntity> johnDoe = agentPersonnelJpaRepository.findByEmail("john.doe@lecese.fr");
        if (johnDoe.isEmpty()) {
            AgentPersonnelEntity agent1 = new AgentPersonnelEntity(
                UUID.randomUUID(),
                "Doe",
                "John",
                "john.doe@lecese.fr",
                roleAdmin,
                EtatAgent.actif
            );
            agentPersonnelJpaRepository.save(agent1);

            // Créer l'agent direction pour John Doe
            AgentDirectionEntity agentDirection1 = new AgentDirectionEntity(
                UUID.randomUUID(),
                null, // codeAgentDirection
                null, // codeAgent
                "direction_rh", // codeDirection
                drhId,
                LocalDate.now(),
                null, // dateDepart
                agent1.getId(),
                null
            );
            agentDirectionJpaRepository.save(agentDirection1);

            logger.info("Agent 'John Doe' créé avec la direction DRH");
        }

        // Agent 2: Jane Doe
        Optional<AgentPersonnelEntity> janeDoe = agentPersonnelJpaRepository.findByEmail("jane.doe@lecese.fr");
        if (janeDoe.isEmpty()) {
            AgentPersonnelEntity agent2 = new AgentPersonnelEntity(
                UUID.randomUUID(),
                "Doe",
                "Jane",
                "jane.doe@lecese.fr",
                roleAdmin,
                EtatAgent.actif
            );
            agentPersonnelJpaRepository.save(agent2);

            // Créer l'agent direction pour Jane Doe
            AgentDirectionEntity agentDirection2 = new AgentDirectionEntity(
                UUID.randomUUID(),
                null, // codeAgentDirection
                null, // codeAgent
                "direction_appi", // codeDirection
                dappiId,
                LocalDate.now(),
                null, // dateDepart
                agent2.getId(),
                null
            );
            agentDirectionJpaRepository.save(agentDirection2);

            logger.info("Agent 'Jane Doe' créé avec la direction DAPPI");
        }

        // Agent 3: Mathieu Dufour
        Optional<AgentPersonnelEntity> mathieuDufour = agentPersonnelJpaRepository.findByEmail("mathieu.dufour@lecese.fr");
        if (mathieuDufour.isEmpty()) {
            AgentPersonnelEntity agent3 = new AgentPersonnelEntity(
                UUID.randomUUID(),
                "Dufour",
                "Mathieu",
                "mathieu.dufour@lecese.fr",
                roleAdmin,
                EtatAgent.actif
            );
            agentPersonnelJpaRepository.save(agent3);

            // Créer l'agent direction pour Mathieu Dufour
            AgentDirectionEntity agentDirection3 = new AgentDirectionEntity(
                UUID.randomUUID(),
                null, // codeAgentDirection
                null, // codeAgent
                "direction_siun", // codeDirection
                dsiunId,
                LocalDate.now(),
                null, // dateDepart
                agent3.getId(),
                null
            );
            agentDirectionJpaRepository.save(agentDirection3);

            logger.info("Agent 'Mathieu Dufour' créé avec la direction DSIUN");
        }

        // Agent 4: Nicolas Martin
        Optional<AgentPersonnelEntity> nicolasMartin = agentPersonnelJpaRepository.findByEmail("nicolas.martin@lecese.fr");
        if (nicolasMartin.isEmpty()) {
            AgentPersonnelEntity agent4 = new AgentPersonnelEntity(
                UUID.randomUUID(),
                "Martin",
                "Nicolas",
                "nicolas.martin@lecese.fr",
                roleAdmin,
                EtatAgent.actif
            );
            agentPersonnelJpaRepository.save(agent4);

            // Créer l'agent direction pour Nicolas Martin
            AgentDirectionEntity agentDirection4 = new AgentDirectionEntity(
                UUID.randomUUID(),
                null, // codeAgentDirection
                null, // codeAgent
                "direction_af", // codeDirection
                dafId,
                LocalDate.now(),
                null, // dateDepart
                agent4.getId(),
                null
            );
            agentDirectionJpaRepository.save(agentDirection4);

            logger.info("Agent 'Nicolas Martin' créé avec la direction DAF");
        }

        // Agent 5: Michel Durand
        Optional<AgentPersonnelEntity> michelDurand = agentPersonnelJpaRepository.findByEmail("michel.durand@lecese.fr");
        if (michelDurand.isEmpty()) {
            AgentPersonnelEntity agent5 = new AgentPersonnelEntity(
                UUID.randomUUID(),
                "Durand",
                "Michel",
                "michel.durand@lecese.fr",
                roleAdmin,
                EtatAgent.actif
            );
            agentPersonnelJpaRepository.save(agent5);

            // Créer l'agent direction pour Michel Durand
            AgentDirectionEntity agentDirection5 = new AgentDirectionEntity(
                UUID.randomUUID(),
                null, // codeAgentDirection
                null, // codeAgent
                "direction_ici", // codeDirection
                diciId,
                LocalDate.now(),
                null, // dateDepart
                agent5.getId(),
                null
            );
            agentDirectionJpaRepository.save(agentDirection5);

            logger.info("Agent 'Michel Durand' créé avec la direction DICI");
        }
    }
}
