package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.AddGroupeToTemplateCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.editor.AddGroupeToTemplateApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates/editor")
public class TemplateEditorController {
    
    private final AddGroupeToTemplateApi addGroupeToTemplateApi;
    
    public TemplateEditorController(AddGroupeToTemplateApi addGroupeToTemplateApi) {
        this.addGroupeToTemplateApi = addGroupeToTemplateApi;
    }
    
    @PostMapping("/add-groupe")
    public ResponseEntity<TemplateProcessusDTO> addGroupeToTemplate(@RequestBody AddGroupeToTemplateCommand command) {
        TemplateProcessusDTO template = addGroupeToTemplateApi.addGroupeToTemplate(command);
        return ResponseEntity.ok(template);
    }
}
