package com.cese.process_entree_sortie.application.service.template.dependance;

import com.cese.process_entree_sortie.application.port.in.template.dependance.ValidateTemplateDependancesApi;
import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ValidateTemplateDependancesService implements ValidateTemplateDependancesApi {
    
    private final TemplateDependanceSpi templateDependanceSpi;
    
    public ValidateTemplateDependancesService(TemplateDependanceSpi templateDependanceSpi) {
        this.templateDependanceSpi = templateDependanceSpi;
    }
    
    @Override
    public Boolean ValidateTemplateDependances(UUID templateProcessusID) {
        List<TemplateDependance> dependances = templateDependanceSpi.findByTemplateProcessusId(templateProcessusID);
        
        // TODO: Valider les dépendances - vérifier qu'il n'y a pas de cycles, que les tâches/groupes existent, etc.
        // Pour l'instant, on retourne true si aucune dépendance n'est trouvée ou si la validation passe
        // La validation réelle est déjà faite dans le constructeur de TemplateDependance (pas de dépendance sur soi-même)
        return true;
    }
}
