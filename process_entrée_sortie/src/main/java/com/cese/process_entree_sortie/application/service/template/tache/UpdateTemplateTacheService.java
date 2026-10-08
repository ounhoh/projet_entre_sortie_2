package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.entree.tache.UpdateTemplateTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.UpdateTemplateTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateTemplateTacheService implements UpdateTemplateTacheApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateTacheConverter converter;
    
    public UpdateTemplateTacheService(TemplateTacheSpi templateTacheSpi, TemplateTacheConverter converter) {
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateTacheDTO udpateTemplateTache(UpdateTemplateTacheCommand command) {
        TemplateTache tache = templateTacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Template tache non trouvée avec l'ID: " + command.tacheId()));
        
        // Créer une nouvelle tache avec les valeurs modifiées
        TemplateTache tacheModifiee = new TemplateTache(
            tache.id(),
            tache.code(),
            command.libelle() != null ? command.libelle() : tache.libelle(),
            command.description() != null ? command.description() : tache.description(),
            tache.type(),
            command.delaiJour(),
            tache.contenu(),
            tache.dependanceId()
        );
        
        TemplateTache tacheSauvegarde = templateTacheSpi.save(tacheModifiee);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
