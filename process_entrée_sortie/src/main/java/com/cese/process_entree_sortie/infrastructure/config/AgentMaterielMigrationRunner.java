package com.cese.process_entree_sortie.infrastructure.config;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentMaterielEtDroitEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.AgentMaterielEtDroitJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AgentMaterielMigrationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AgentMaterielMigrationRunner.class);

    private final AgentMaterielEtDroitJpaRepository repository;
    private final ObjectMapper objectMapper;

    public AgentMaterielMigrationRunner(AgentMaterielEtDroitJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<AgentMaterielEtDroitEntity> entities = repository.findAll();
        if (entities.isEmpty()) {
            logger.info("[MigrationMateriel] Aucun enregistrement à migrer");
            return;
        }

        int migrated = 0;
        for (AgentMaterielEtDroitEntity entity : entities) {
            String json = entity.getMaterielEtDroit();
            if (json == null || json.isBlank()) {
                continue;
            }

            try {
                Map<String, Object> raw = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                Map<String, Object> normalized = normalizeMaterielMap(raw);
                if (normalized.equals(raw)) {
                    continue;
                }

                entity.setMaterielEtDroit(objectMapper.writeValueAsString(normalized));
                repository.save(entity);
                migrated++;
                logger.info("[MigrationMateriel] Agent {} migré vers format {materiels, droits}", entity.getAgentId());
            } catch (Exception e) {
                logger.warn("[MigrationMateriel] Échec de migration pour l'agent {}: {}", entity.getAgentId(), e.getMessage());
            }
        }

        logger.info("[MigrationMateriel] Migration terminée: {} enregistrement(s) mis à jour", migrated);
    }

    private Map<String, Object> normalizeMaterielMap(Map<String, Object> raw) {
        Map<String, Object> result = new HashMap<>();
        List<String> materielsList = new ArrayList<>();
        List<String> droitsList = new ArrayList<>();

        Object materielObj = raw.getOrDefault("materiels", raw.get("materials"));
        if (materielObj == null) {
            materielObj = raw.getOrDefault("materiel", raw.get("material"));
        }
        Object droitsObj = raw.getOrDefault("droits", raw.get("droit"));
        if (droitsObj == null && raw.containsKey("droitAgent")) {
            droitsObj = raw.get("droitAgent");
        }

        if (materielObj instanceof Map) {
            Map<?, ?> rawMateriel = (Map<?, ?>) materielObj;
            for (Map.Entry<?, ?> entry : rawMateriel.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String key = entry.getKey().toString().trim();
                if (!key.isBlank()) {
                    materielsList.add(key);
                }
            }
        } else if (materielObj instanceof List) {
            List<?> materialList = (List<?>) materielObj;
            for (Object item : materialList) {
                if (item != null && !item.toString().isBlank()) {
                    materielsList.add(item.toString().trim());
                }
            }
        } else if (!raw.containsKey("materiels") && !raw.containsKey("materials")
            && !raw.containsKey("materiel") && !raw.containsKey("material")) {
            // Ancien format flat: chaque clé est un matériel
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String key = entry.getKey();
                if (isReservedKey(key)) {
                    continue;
                }
                if (key != null && !key.isBlank()) {
                    materielsList.add(key.trim());
                }
            }
        }

        appendDroits(droitsObj, droitsList);

        result.put("materiels", materielsList);
        result.put("droits", droitsList);
        return result;
    }

    private boolean isReservedKey(String key) {
        return "droits".equals(key) || "droit".equals(key) || "droitAgent".equals(key)
            || "materiel".equals(key) || "material".equals(key)
            || "materiels".equals(key) || "materials".equals(key);
    }

    private void appendDroits(Object droitsObj, List<String> droitsList) {
        if (droitsObj instanceof List) {
            List<?> list = (List<?>) droitsObj;
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    droitsList.add(item.toString());
                }
            }
        } else if (droitsObj instanceof String droitStr && !droitStr.isBlank()) {
            droitsList.add(droitStr);
        } else if (droitsObj instanceof Map) {
            Map<?, ?> droitsMap = (Map<?, ?>) droitsObj;
            for (Object key : droitsMap.keySet()) {
                if (key != null && !key.toString().isBlank()) {
                    droitsList.add(key.toString());
                }
            }
        }
    }
}
