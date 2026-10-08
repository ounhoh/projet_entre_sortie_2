package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.CreateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.CreateTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CreateTemplateProcessusService implements CreateTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public CreateTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO createTemplateProcessus(CreateTemplateProcessusCommand command) {
        // Convertir les StatutProcessusDTO en StatutProcessus
        java.util.List<StatutProcessus> statuts = new ArrayList<>();
        if (command.statutProcessusDTOList() != null) {
            statuts = command.statutProcessusDTOList().stream()
                .map(dto -> new StatutProcessus(dto.id(), dto.code(), dto.libelle()))
                .collect(Collectors.toList());
        }
        
        // Créer le template avec le Builder
        TemplateProcessus nouveauTemplate = TemplateProcessus.Builder()
            .withId(UUID.randomUUID())
            .withCodeProcess(command.code())
            .withLibProcess(command.libelle())
            .withDescription(command.description())
            .withTypeProcess(TypeProcessus.valueOf(command.type()))
            .withEtatActif(false) // Par défaut inactif
            .withStatutProcess(statuts)
            .withGroupeTacheList(new ArrayList<>()) // Groupes vides au départ, à ajouter séparément
            .build();
        
        TemplateProcessus templateSauvegarde = templateProcessusSpi.save(nouveauTemplate);
        return converter.convertirEnDTO(templateSauvegarde);
    }
}
