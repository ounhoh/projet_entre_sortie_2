package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TacheConverter {
    
    private final TemplateTacheSpi templateTacheSpi;
    
    public TacheConverter(TemplateTacheSpi templateTacheSpi) {
        this.templateTacheSpi = templateTacheSpi;
    }
    
    public TacheDTO convertirEnDTO(InstanceTache tache) {
        // Convertir TacheContenu en Map
        Map<String, Object> contenu = new HashMap<>();
        Map<String, Object> reponses = new HashMap<>();
        
        if (tache.contenu() != null && tache.contenu().contenuTache() != null) {
            Map<String, Object> contenuComplet = tache.contenu().contenuTache();
            
            // Séparer la structure (champs, actions) des réponses utilisateur
            // Les réponses peuvent être soit dans une clé "reponses", soit mêlées avec les autres clés
            if (contenuComplet.containsKey("reponses") && contenuComplet.get("reponses") instanceof Map) {
                // Cas où les réponses sont déjà séparées
                @SuppressWarnings("unchecked")
                Map<String, Object> reponsesMap = (Map<String, Object>) contenuComplet.get("reponses");
                reponses.putAll(reponsesMap);
                
                // Garder seulement la structure (champs, actions) dans contenu
                Map<String, Object> contenuStructure = new HashMap<>(contenuComplet);
                contenuStructure.remove("reponses");
                contenu = contenuStructure;
            } else {
                // Cas legacy : extraire les réponses des champs du formulaire
                // Les réponses sont les clés qui correspondent aux IDs des champs du formulaire
                // et qui ne sont pas des clés système (champs, actions)
                contenu = new HashMap<>(contenuComplet);
                
                // Extraire les réponses : toutes les clés sauf celles qui sont des clés système
                var clesSysteme = java.util.Set.of("champs", "actions", "fields");
                for (Map.Entry<String, Object> entry : contenuComplet.entrySet()) {
                    if (!clesSysteme.contains(entry.getKey().toLowerCase())) {
                        // C'est probablement une réponse utilisateur
                        reponses.put(entry.getKey(), entry.getValue());
                    }
                }
                
                // Retirer les réponses du contenu structure pour garder seulement champs/actions
                for (String cleReponse : reponses.keySet()) {
                    contenu.remove(cleReponse);
                }
            }
        }
        
        // Récupérer la description et le type depuis le template
        var templateOpt = templateTacheSpi.findById(tache.templateId());
        String description = templateOpt
            .map(template -> template.description())
            .orElse(null);
        com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType type = templateOpt
            .map(template -> template.type())
            .orElse(com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType.tache);
        
        return new TacheDTO(
            tache.id(),
            tache.code(),
            tache.libelle(),
            tache.statut(),
            description,
            contenu,
            reponses.isEmpty() ? null : reponses, // null si pas de réponses pour éviter les Maps vides
            tache.dateEcheance(),
            type
        );
    }
}
