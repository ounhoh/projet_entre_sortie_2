import apiClient from '@/lib/axios';

// Type pour TacheDTO
export type TacheDTO = {
  id: string;
  code: string;
  libelle: string;
  statut: 'enAttente' | 'enCours' | 'fait' | 'bloque';
  description: string | null;
  contenu: Record<string, any> | null; // Map<String, Object> - contient la structure du formulaire (champs, actions)
  reponses?: Record<string, any> | null; // Map<String, Object> - contient les réponses de l'utilisateur
  dateEcheance: string; // ISO date string
  type?: string; // 'tache', 'formulaire', 'groupeTache', etc.
};

// Service pour les tâches
export const tacheService = {
  // Récupérer toutes les tâches d'un processus
  async getTachesByProcessus(processusId: string): Promise<TacheDTO[]> {
    const response = await apiClient.get<TacheDTO[]>(`/api/taches/processus/${processusId}`);
    return response.data;
  },

  // Récupérer une tâche par ID
  async getTacheById(id: string): Promise<TacheDTO> {
    const response = await apiClient.get<TacheDTO>(`/api/taches/${id}`);
    return response.data;
  },

  // Compléter une tâche
  async completeTache(id: string): Promise<TacheDTO> {
    console.log('[tacheService] completeTache payload:', { id });
    const response = await apiClient.put<TacheDTO>(`/api/taches/${id}/complete`);
    return response.data;
  },

  // Annuler la validation d'une tâche (remet toutes les tâches du groupe à "à faire")
  async annulerValidationTache(id: string): Promise<TacheDTO> {
    const response = await apiClient.put<TacheDTO>(`/api/taches/${id}/annuler-validation`);
    return response.data;
  },

  // Soumettre un formulaire (sauvegarde les réponses dans "reponses")
  async submitFormulaire(id: string, contenujson: Record<string, any>): Promise<TacheDTO> {
    console.log('[tacheService] submitFormulaire payload:', { id, contenujson });
    const response = await apiClient.post<TacheDTO>(`/api/taches/${id}/submit-formulaire`, {
      tacheId: id,
      contenujson: contenujson
    });
    return response.data;
  },
};
