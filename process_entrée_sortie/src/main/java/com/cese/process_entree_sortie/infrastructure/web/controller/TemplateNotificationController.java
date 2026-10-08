/* 

package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.template.entree.notification.CreateTemplateNotificationCommand;
import com.cese.process_entree_sortie.application.dto.template.entree.notification.UpdateTemplateNotificationCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.application.port.in.template.notification.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates/notifications")
public class TemplateNotificationController {
    
    private final CreateTemplateNotificationApi createTemplateNotificationApi;
    private final GetTemplateNotificationByIdApi getTemplateNotificationByIdApi;
    private final ListTemplateNotificationApi listTemplateNotificationApi;
    private final UpdateTemplateNotificationApi updateTemplateNotificationApi;
    private final DeleteTemplateNotificationApi deleteTemplateNotificationApi;
    private final GetTemplatesByNiveauApi getTemplatesByNiveauApi;
    
    public TemplateNotificationController(
            CreateTemplateNotificationApi createTemplateNotificationApi,
            GetTemplateNotificationByIdApi getTemplateNotificationByIdApi,
            ListTemplateNotificationApi listTemplateNotificationApi,
            UpdateTemplateNotificationApi updateTemplateNotificationApi,
            DeleteTemplateNotificationApi deleteTemplateNotificationApi,
            GetTemplatesByNiveauApi getTemplatesByNiveauApi) {
        this.createTemplateNotificationApi = createTemplateNotificationApi;
        this.getTemplateNotificationByIdApi = getTemplateNotificationByIdApi;
        this.listTemplateNotificationApi = listTemplateNotificationApi;
        this.updateTemplateNotificationApi = updateTemplateNotificationApi;
        this.deleteTemplateNotificationApi = deleteTemplateNotificationApi;
        this.getTemplatesByNiveauApi = getTemplatesByNiveauApi;
    }
    
    @PostMapping
    public ResponseEntity<TemplateNotificationDTO> createTemplateNotification(@RequestBody CreateTemplateNotificationCommand command) {
        TemplateNotificationDTO notification = createTemplateNotificationApi.createNotification(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateNotificationDTO> getTemplateNotificationById(@PathVariable UUID id) {
        TemplateNotificationDTO notification = getTemplateNotificationByIdApi.getTemplateNotificationById(id);
        return ResponseEntity.ok(notification);
    }
    
    @GetMapping
    public ResponseEntity<List<TemplateNotificationDTO>> listTemplateNotifications() {
        List<TemplateNotificationDTO> notifications = listTemplateNotificationApi.getListTemplateNotification();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<TemplateNotificationDTO>> getTemplatesByNiveau(@PathVariable String niveau) {
        List<TemplateNotificationDTO> notifications = getTemplatesByNiveauApi.getTemplatesByNiveau(niveau);
        return ResponseEntity.ok(notifications);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemplateNotificationDTO> updateTemplateNotification(
            @PathVariable UUID id,
            @RequestBody UpdateTemplateNotificationCommand command) {
        TemplateNotificationDTO notification = updateTemplateNotificationApi.updateTemplateNotification(command);
        return ResponseEntity.ok(notification);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateNotification(@PathVariable UUID id) {
        deleteTemplateNotificationApi.deleteTemplateNotificationbyId(id);
        return ResponseEntity.noContent().build();
    }
}

*/