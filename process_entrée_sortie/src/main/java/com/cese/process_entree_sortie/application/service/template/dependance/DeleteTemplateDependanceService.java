package com.cese.process_entree_sortie.application.service.template.dependance;

import com.cese.process_entree_sortie.application.port.in.template.dependance.DeleteTemplateDependanceApi;
import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteTemplateDependanceService implements DeleteTemplateDependanceApi {
    
    private final TemplateDependanceSpi templateDependanceSpi;
    
    public DeleteTemplateDependanceService(TemplateDependanceSpi templateDependanceSpi) {
        this.templateDependanceSpi = templateDependanceSpi;
    }
    
    @Override
    public void deleteTemplateDependance(UUID dependanceId) {
        TemplateDependance dependance = templateDependanceSpi.findById(dependanceId)
            .orElseThrow(() -> new RuntimeException("Template dependance non trouvée avec l'ID: " + dependanceId));
        templateDependanceSpi.delete(dependance);
    }
}
