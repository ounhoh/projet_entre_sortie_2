import apiClient from '@/lib/axios';

// Types correspondant aux DTOs du backend
export type StatutProcessusDTO = {
  id: string;
  code: string;
  libelle: string;
};

export type AgentDTO = {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  role: string;
  code: string;
  libelle: string;
  service: string;
  direction: string;
  agentResponsable: string | null;
  dateArrivee?: string | null; // ISO date string ou null
  dateSortie: string | null; // ISO date string ou null
  etatAgent: 'entree' | 'actif' | 'sortie' | 'mobiliteInterne' | 'ancien';
  numeroBureau: string | null;
};

export type ProcessusDTO = {
  id: string;
  code: string;
  statut: StatutProcessusDTO;
  labelleDirectionConcernee: string;
  agent: AgentDTO;
  typeProcessus: 'entree' | 'sortie' | 'mobitliteInterne';
  dateMobilite?: string | null; // ISO date string
  dateArrivee: string; // ISO date string
  dateEcheance: string; // ISO date string
  templateId: string; // UUID du template
};

// DTOs détaillés
export type TacheDTO = {
  id: string;
  code: string;
  libelle: string;
  statut: string;
  description: string | null;
  contenu: Record<string, any> | null; // Structure du formulaire (champs, actions)
  reponses?: Record<string, any> | null; // Réponses de l'utilisateur
  dateEcheance: string | null;
  type?: string; // 'tache', 'formulaire', 'groupeTache', etc.
};

export type GroupeTacheDTO = {
  id: string;
  code: string;
  libelle: string;
  statut: string;
  dateEcheance: string | null;
  taches: TacheDTO[];
  templateId: string; // UUID du template groupe
};

export type ProcessDetailDTO = {
  processInfo: ProcessusDTO;
  groupeTacheList: GroupeTacheDTO[];
  dependanceList: DependanceDTO[];
};

export type DependanceDTO = {
  id: string;
  sourceTacheId: string;
  cibleTacheIds: string[];
};

// Service pour les processus
export const processusService = {
  // Récupérer tous les processus actifs
  async getProcessusActifs(): Promise<ProcessusDTO[]> {
    const response = await apiClient.get<ProcessusDTO[]>('/api/processus/actifs');
    return response.data;
  },

  // Récupérer un processus par ID
  async getProcessusById(id: string): Promise<ProcessusDTO> {
    const response = await apiClient.get<ProcessusDTO>(`/api/processus/${id}`);
    return response.data;
  },

  // Récupérer un processus avec ses groupes et dépendances
  async getProcessusDetails(id: string): Promise<ProcessDetailDTO> {
    const response = await apiClient.get<ProcessDetailDTO>(`/api/processus/${id}/details`);
    return response.data;
  },

  // Récupérer les processus par agent
  async getProcessusByAgent(agentId: string): Promise<ProcessusDTO[]> {
    const response = await apiClient.get<ProcessusDTO[]>(`/api/processus/agent/${agentId}`);
    return response.data;
  },

  // Récupérer les processus en retard
  async getProcessusEnRetard(): Promise<ProcessusDTO[]> {
    const response = await apiClient.get<ProcessusDTO[]>('/api/processus/en-retard');
    return response.data;
  },

  // Démarrer un processus
  async startProcessus(id: string): Promise<ProcessusDTO> {
    const response = await apiClient.put<ProcessusDTO>(`/api/processus/${id}/start`);
    return response.data;
  },

  // Annuler un processus
  async cancelProcessus(id: string): Promise<ProcessusDTO> {
    const response = await apiClient.put<ProcessusDTO>(`/api/processus/${id}/cancel`);
    return response.data;
  },

  // Compléter un processus
  async completeProcessus(id: string): Promise<ProcessusDTO> {
    const response = await apiClient.put<ProcessusDTO>(`/api/processus/${id}/complete`);
    return response.data;
  },

  // Créer un processus depuis un template
  async createProcessus(command: {
    agentId?: string;
    templateProcessusId: string;
    dateDebut?: string; // ISO date string
    formulaireData?: Record<string, any>;
  }): Promise<ProcessusDTO> {
    console.log('[processusService] createProcessus payload:', command);
    const response = await apiClient.post<ProcessusDTO>('/api/processus', command);
    return response.data;
  },
};