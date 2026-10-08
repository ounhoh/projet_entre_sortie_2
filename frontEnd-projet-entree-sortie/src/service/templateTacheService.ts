import apiClient from '@/lib/axios';

// Type pour TemplateTacheDTO
export type TemplateTacheDTO = {
  id: string;
  libelle: string;
  code: string;
  description: string;
  contenu: Record<string, any> | string; // Map<String, Object> ou chaîne JSON - contient la définition du formulaire
};

// Service pour les templates de tâches
export const templateTacheService = {
  // Récupérer un template de tâche par ID
  async getTemplateTacheById(id: string): Promise<TemplateTacheDTO> {
    const response = await apiClient.get<TemplateTacheDTO>(`/api/templates/taches/${id}`);
    return response.data;
  },

  // Récupérer toutes les tâches d'un groupe de tâches
  async getTachesByGroupeTache(groupeId: string): Promise<TemplateTacheDTO[]> {
    const response = await apiClient.get<TemplateTacheDTO[]>(`/api/templates/taches/groupe/${groupeId}`);
    return response.data;
  },
};
