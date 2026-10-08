package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.UpdateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.UpdateTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional
public class UpdateTemplateProcessusService implements UpdateTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public UpdateTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO updateTemplateProcessus(UpdateTemplateProcessusCommand command) {
        TemplateProcessus template = templateProcessusSpi.findById(command.templateId())
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + command.templateId()));
        
        // Convertir les StatutProcessusDTO en StatutProcessus
        java.util.List<StatutProcessus> statuts = new ArrayList<>();
        if (command.statutList() != null) {
            statuts = command.statutList().stream()
                .map(dto -> new StatutProcessus(dto.id(), dto.code(), dto.libelle()))
                .collect(Collectors.toList());
        }
        
        // Utiliser le Builder pour créer un nouveau template modifié
        TemplateProcessus templateModifie = TemplateProcessus.builder(template)
            .withLibProcess(command.libelle())
            .withDescription(command.description())
            .withStatutProcess(statuts)
            .withGroupeTacheList(new ArrayList<>()) // TODO: Convertir groupeTaches depuis DTOs
            .build();
        
        TemplateProcessus templateSauvegarde = templateProcessusSpi.save(templateModifie);
        return converter.convertirEnDTO(templateSauvegarde);
    }
}
