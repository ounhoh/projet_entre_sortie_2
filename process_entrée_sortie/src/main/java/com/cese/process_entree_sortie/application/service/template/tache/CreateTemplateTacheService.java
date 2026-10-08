package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.entree.tache.CreateTemplateTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.CreateTemplateTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TacheContenu;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

@Service
@Transactional
public class CreateTemplateTacheService implements CreateTemplateTacheApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateTacheConverter converter;
    
    public CreateTemplateTacheService(TemplateTacheSpi templateTacheSpi, TemplateTacheConverter converter) {
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateTacheDTO createTemplateTache(CreateTemplateTacheCommand command) {
        // Créer un nouveau template tache
        TemplateTache nouvelleTache = new TemplateTache(
            UUID.randomUUID(),
            command.code(),
            command.libelle(),
            command.description(),
            command.type(),
            command.delaijour(),
            new TacheContenu(new HashMap<>()), // Contenu vide par défaut
            command.dependanceId()
        );
        
        TemplateTache tacheSauvegarde = templateTacheSpi.save(nouvelleTache);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
