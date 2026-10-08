package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.notification.entree.SendNotificationCommand;
import com.cese.process_entree_sortie.application.dto.notification.entree.SendReminderCommand;
import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.in.notification.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final SendNotificationApi sendNotificationApi;
    private final GetNotificationByIdApi getNotificationByIdApi;
    private final ListNotificationsByAgentApi listNotificationsByAgentApi;
    private final ListUnreadNotificationApi listUnreadNotificationApi;
    private final MarkNotificationAsReadApi markNotificationAsReadApi;
    private final MarkAllNotificationReadApi markAllNotificationReadApi;
    private final CountUnreadNotificationApi countUnreadNotificationApi;
    private final DeleteNotificationApi deleteNotificationApi;
    private final SendReminderApi sendReminderApi;
    
    public NotificationController(
            SendNotificationApi sendNotificationApi,
            GetNotificationByIdApi getNotificationByIdApi,
            ListNotificationsByAgentApi listNotificationsByAgentApi,
            ListUnreadNotificationApi listUnreadNotificationApi,
            MarkNotificationAsReadApi markNotificationAsReadApi,
            MarkAllNotificationReadApi markAllNotificationReadApi,
            CountUnreadNotificationApi countUnreadNotificationApi,
            DeleteNotificationApi deleteNotificationApi,
            SendReminderApi sendReminderApi) {
        this.sendNotificationApi = sendNotificationApi;
        this.getNotificationByIdApi = getNotificationByIdApi;
        this.listNotificationsByAgentApi = listNotificationsByAgentApi;
        this.listUnreadNotificationApi = listUnreadNotificationApi;
        this.markNotificationAsReadApi = markNotificationAsReadApi;
        this.markAllNotificationReadApi = markAllNotificationReadApi;
        this.countUnreadNotificationApi = countUnreadNotificationApi;
        this.deleteNotificationApi = deleteNotificationApi;
        this.sendReminderApi = sendReminderApi;
    }
    
    @PostMapping
    public ResponseEntity<NotificationDTO> sendNotification(@RequestBody SendNotificationCommand command) {
        NotificationDTO notification = sendNotificationApi.sendNotification(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable UUID id) {
        NotificationDTO notification = getNotificationByIdApi.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<NotificationDTO>> listNotificationsByAgent(@PathVariable UUID agentId) {
        List<NotificationDTO> notifications = listNotificationsByAgentApi.getListNotificationByAgent(agentId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/agent/{agentId}/non-lues")
    public ResponseEntity<List<NotificationDTO>> listUnreadNotifications(@PathVariable UUID agentId) {
        List<NotificationDTO> notifications = listUnreadNotificationApi.getListUnreadNotificationApi(agentId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/agent/{agentId}/count-non-lues")
    public ResponseEntity<Integer> countUnreadNotifications(@PathVariable UUID agentId) {
        int count = countUnreadNotificationApi.countUnreadNotification(agentId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/marquer-lu")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable UUID id) {
        markNotificationAsReadApi.markNotificationAsREed(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/agent/{agentId}/marquer-toutes-lues")
    public ResponseEntity<Void> markAllNotificationsRead(@PathVariable UUID agentId) {
        markAllNotificationReadApi.markAllNotificationRead(agentId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        deleteNotificationApi.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/rappel")
    public ResponseEntity<NotificationDTO> sendReminder(@RequestBody SendReminderCommand command) {
        NotificationDTO notification = sendReminderApi.sendReminder(command);
        return ResponseEntity.ok(notification);
    }
}
