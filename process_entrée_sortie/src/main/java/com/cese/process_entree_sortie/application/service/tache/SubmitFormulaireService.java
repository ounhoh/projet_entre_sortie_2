package com.cese.process_entree_sortie.application.service.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.SubmitFormulaireCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import com.cese.process_entree_sortie.application.port.in.tache.SubmitFormulaireApi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class SubmitFormulaireService implements SubmitFormulaireApi {
    
    private final TacheSpi tacheSpi;
    private final TacheConverter converter;
    
    public SubmitFormulaireService(TacheSpi tacheSpi, TacheConverter converter) {
        this.tacheSpi = tacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TacheDTO submitFormulaire(SubmitFormulaireCommand command) {
        InstanceTache tache = tacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Tache non trouvée avec l'ID: " + command.tacheId()));
        
        // Séparer la structure (champs, actions) des réponses utilisateur
        Map<String, Object> nouveauContenuMap = new java.util.HashMap<>();
        Map<String, Object> reponsesMap = new java.util.HashMap<>();
        Map<String, Object> structureMap = new java.util.HashMap<>();
        
        // Clés système qui font partie de la structure du formulaire
        var clesSysteme = java.util.Set.of("champs", "actions", "fields");
        
        // Séparer les données entrantes
        if (command.contenujson() != null) {
            for (Map.Entry<String, Object> entry : command.contenujson().entrySet()) {
                String cle = entry.getKey();
                if (clesSysteme.contains(cle.toLowerCase())) {
                    // C'est une clé de structure (champs, actions)
                    structureMap.put(cle, entry.getValue());
                } else {
                    // C'est probablement une réponse utilisateur
                    reponsesMap.put(cle, entry.getValue());
                }
            }
        }
        
        // Conserver la structure existante si elle existe et n'est pas fournie dans les nouvelles données
        if (tache.contenu() != null && tache.contenu().contenuTache() != null) {
            Map<String, Object> contenuExistant = tache.contenu().contenuTache();
            for (String cleSysteme : clesSysteme) {
                if (contenuExistant.containsKey(cleSysteme) && !structureMap.containsKey(cleSysteme)) {
                    // Conserver la structure existante si elle n'est pas remplacée
                    structureMap.put(cleSysteme, contenuExistant.get(cleSysteme));
                }
            }
        }
        
        // Combiner structure et réponses dans le nouveau contenu
        nouveauContenuMap.putAll(structureMap);
        if (!reponsesMap.isEmpty()) {
            nouveauContenuMap.put("reponses", reponsesMap);
        }
        
        // Mettre à jour le contenu de la tâche avec les données organisées
        TacheContenu nouveauContenu = new TacheContenu(nouveauContenuMap);
        InstanceTache tacheModifiee = new InstanceTache(
            tache.id(),
            tache.code(),
            tache.libelle(),
            nouveauContenu,
            tache.statut(),
            tache.dateEcheance(),
            tache.templateId(),
            tache.groupeTacheId(),
            tache.dependanceId() // Conserver le dependanceId
        );
        
        InstanceTache tacheSauvegarde = tacheSpi.save(tacheModifiee);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
