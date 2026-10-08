import apiClient from '@/lib/axios';
import { agentService } from './agentService';

// Type correspondant au NotificationDTO du backend
export type NotificationDTO = {
  id: string;
  niveauNotification: 'alerte' | 'information' | 'rappel';
  object: string;
  dateEnvoie: string; // ISO date string
  directionEnvoyeur: string | null;
  agentAyantRecuNotificaiton: string[]; // Liste des UUIDs des agents
  processusId: string | null; // UUID du processus lié
};

// Type pour les notifications formatées pour le frontend
export type NotificationFormatted = {
  id: string;
  Niveau: 'tache' | 'alerte' | 'information' | 'rappel'; // 'tache' pour compatibilité, mais backend utilise 'rappel'
  dateEnvoie: string;
  Direction: string;
  agent: {
    nomPrenom: string;
    direction: string;
  };
  Description: string;
  processusId?: string;
};

// Service pour les notifications
export const notificationService = {
  // Récupérer toutes les notifications d'un agent
  async getNotificationsByAgent(agentId: string): Promise<NotificationFormatted[]> {
    const response = await apiClient.get<NotificationDTO[]>(`/api/notifications/agent/${agentId}`);
    return this.formatNotifications(response.data);
  },

  // Récupérer les notifications non lues d'un agent
  async getUnreadNotificationsByAgent(agentId: string): Promise<NotificationFormatted[]> {
    const response = await apiClient.get<NotificationDTO[]>(`/api/notifications/agent/${agentId}/non-lues`);
    return this.formatNotifications(response.data);
  },

  // Compter les notifications non lues
  async countUnreadNotifications(agentId: string): Promise<number> {
    const response = await apiClient.get<number>(`/api/notifications/agent/${agentId}/count-non-lues`);
    return response.data;
  },

  // Marquer une notification comme lue
  async markNotificationAsRead(notificationId: string): Promise<void> {
    await apiClient.put(`/api/notifications/${notificationId}/marquer-lu`);
  },

  // Marquer toutes les notifications d'un agent comme lues
  async markAllNotificationsRead(agentId: string): Promise<void> {
    await apiClient.put(`/api/notifications/agent/${agentId}/marquer-toutes-lues`);
  },

  // Supprimer une notification
  async deleteNotification(notificationId: string): Promise<void> {
    await apiClient.delete(`/api/notifications/${notificationId}`);
  },

  // Formater les notifications du backend vers le format frontend
  async formatNotifications(notifications: NotificationDTO[]): Promise<NotificationFormatted[]> {
    const formatted: NotificationFormatted[] = [];
    
    // Récupérer tous les IDs d'agents uniques pour optimiser les appels
    const uniqueAgentIds = new Set<string>();
    notifications.forEach(notif => {
      notif.agentAyantRecuNotificaiton.forEach(agentId => uniqueAgentIds.add(agentId));
    });
    
    // Récupérer toutes les informations des agents en parallèle
    const agentMap = new Map<string, any>();
    const agentPromises = Array.from(uniqueAgentIds).map(async (agentId) => {
      try {
        const agent = await agentService.getAgentById(agentId);
        agentMap.set(agentId, agent);
      } catch (error) {
        console.error(`Erreur lors de la récupération de l'agent ${agentId}:`, error);
        agentMap.set(agentId, null);
      }
    });
    await Promise.all(agentPromises);
    
    // Formater les notifications
    for (const notif of notifications) {
      // Convertir 'rappel' en 'tache' pour compatibilité avec le frontend
      const niveau: 'tache' | 'alerte' | 'information' = 
        notif.niveauNotification === 'rappel' ? 'tache' : notif.niveauNotification;
      
      // Pour chaque notification, créer une entrée par agent destinataire
      for (const agentId of notif.agentAyantRecuNotificaiton) {
        const agent = agentMap.get(agentId);
        const nomPrenom = agent 
          ? `${agent.prenom} ${agent.nom}`
          : `Agent ${agentId.substring(0, 8)}`;
        const direction = agent?.direction || 'Non assigné';
        
        formatted.push({
          id: `${notif.id}_${agentId}`, // ID unique par notification-agent
          Niveau: niveau,
          dateEnvoie: new Date(notif.dateEnvoie).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: '2-digit'
          }),
          Direction: notif.directionEnvoyeur || direction,
          agent: {
            nomPrenom: nomPrenom,
            direction: direction
          },
          Description: notif.object,
          processusId: notif.processusId || undefined
        });
      }
    }
    
    return formatted;
  }
};
