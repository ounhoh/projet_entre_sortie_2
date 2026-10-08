import apiClient from '@/lib/axios';
import type { AgentDTO } from './processusService';

// Cache simple pour éviter les appels multiples identiques
type CacheEntry = {
  data: AgentDTO[];
  timestamp: number;
};

const cache = new Map<string, CacheEntry>();
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

// Fonction pour créer une clé de cache à partir des paramètres
function getCacheKey(page: number, size: number, sortBy: string, direction: string): string {
  return `${page}_${size}_${sortBy}_${direction}`;
}

type AgentMaterielPayload = Record<string, any>;

function normalizeMaterielPayload(payload: AgentMaterielPayload | null | undefined): AgentMaterielPayload {
  if (!payload) return {};
  return payload.agentMaterileEtDroit || payload.agentMaterielEtDroit || payload;
}

function parseArrayValue(value: any): string[] {
  if (!value) return [];
  if (Array.isArray(value)) {
    if (value.length === 1 && typeof value[0] === 'string' && value[0].trim().startsWith('[')) {
      try {
        const parsed = JSON.parse(value[0]);
        return Array.isArray(parsed) ? parsed.map(String) : value.map(String);
      } catch {
        return value.map(String);
      }
    }
    return value.map(String);
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed.startsWith('[')) {
      try {
        const parsed = JSON.parse(trimmed);
        return Array.isArray(parsed) ? parsed.map(String) : [value];
      } catch {
        return [value];
      }
    }
    return [value];
  }
  return [];
}

function extractFirstArray(payload: AgentMaterielPayload, keys: string[]): string[] {
  for (const key of keys) {
    if (payload[key] !== undefined) {
      const value = payload[key];
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        return Object.keys(value).map(String);
      }
      return parseArrayValue(value);
    }
  }
  return [];
}

function isAgentActif(agent: AgentDTO): boolean {
  const etat = String(agent.etatAgent || '').trim().toLowerCase();
  if (etat) {
    if (etat === 'actif') return true;
    if (etat === 'ancien') return false;
  }
  const dateSortie = agent.dateSortie;
  if (!dateSortie) return true;
  const parsed = new Date(dateSortie);
  if (Number.isNaN(parsed.getTime())) {
    return true;
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  parsed.setHours(0, 0, 0, 0);
  return parsed.getTime() >= today.getTime();
}

// Service pour les agents
export const agentService = {
  // Récupérer tous les agents (avec pagination)
  async getAgents(
    page: number = 0,
    size: number = 100,
    sortBy: string = 'nom',
    direction: string = 'ASC'
  ): Promise<AgentDTO[]> {
    // Vérifier le cache
    const cacheKey = getCacheKey(page, size, sortBy, direction);
    const cached = cache.get(cacheKey);
    
    if (cached && (Date.now() - cached.timestamp) < CACHE_DURATION) {
      // Retourner les données du cache
      return Promise.resolve(cached.data);
    }
    
    // Appel API si pas de cache valide
    const response = await apiClient.get<AgentDTO[]>('/api/agents', {
      params: {
        page,
        size,
        sortBy,
        direction,
      },
    });
    
    // Mettre en cache
    cache.set(cacheKey, {
      data: response.data,
      timestamp: Date.now(),
    });
    
    return response.data;
  },

  // Récupérer tous les agents (toutes les pages)
  async getAllAgents(
    size: number = 200,
    sortBy: string = 'nom',
    direction: string = 'ASC'
  ): Promise<AgentDTO[]> {
    const all: AgentDTO[] = [];
    const seen = new Set<string>();
    let page = 0;
    while (true) {
      const batch = await this.getAgents(page, size, sortBy, direction);
      console.log('[getAllAgents] page:', page, 'size:', size, 'batch:', batch.length);
      let newCount = 0;
      for (const agent of batch) {
        if (!seen.has(agent.id)) {
          seen.add(agent.id);
          all.push(agent);
          newCount += 1;
        }
      }
      if (batch.length === 0 || newCount === 0) {
        break;
      }
      page += 1;
    }
    return all;
  },

  // Récupérer tous les agents actifs (toutes les pages)
  async getAllAgentsActifs(
    size: number = 200,
    sortBy: string = 'nom',
    direction: string = 'ASC'
  ): Promise<AgentDTO[]> {
    const all = await this.getAllAgents(size, sortBy, direction);
    const etats = Array.from(new Set(all.map(agent => String(agent.etatAgent || '').trim()))).sort();
    console.log('[getAllAgentsActifs] total:', all.length, 'etats:', etats);
    return all.filter(isAgentActif);
  },

  // Récupérer un agent par ID
  async getAgentById(id: string): Promise<AgentDTO> {
    const response = await apiClient.get<AgentDTO>(`/api/agents/${id}`);
    return response.data;
  },

  // Récupérer un agent par code
  async getAgentByCode(code: string): Promise<AgentDTO> {
    const response = await apiClient.get<AgentDTO>(`/api/agents/code/${code}`);
    return response.data;
  },

  // Récupérer le matériel et droits d'un agent
  async getAgentMateriel(id: string): Promise<Record<string, any>> {
    const response = await apiClient.get<Record<string, any>>(`/api/agents/${id}/materiel`);
    return response.data;
  },

  // Récupérer uniquement les droits de l'agent
  async getAgentDroits(id: string): Promise<string[]> {
    const data = normalizeMaterielPayload(await this.getAgentMateriel(id));
    return extractFirstArray(data, ['droitAgent', 'droitsAgent', 'droits']);
  },

  // Récupérer uniquement le matériel de l'agent
  async getAgentMaterielOnly(id: string): Promise<string[]> {
    const data = normalizeMaterielPayload(await this.getAgentMateriel(id));
    return extractFirstArray(data, ['materiels', 'materials', 'materiel', 'materielAgent', 'material']);
  },

  // Récupérer uniquement la diffusion de l'agent
  async getAgentDiffusions(id: string): Promise<string[]> {
    const response = await apiClient.get<string[]>(`/api/agents/${id}/diffusion`);
    return response.data;
  },

  // Récupérer l'affectation active de l'agent
  async getAffectationActive(id: string): Promise<Record<string, any> | null> {
    try {
      const response = await apiClient.get<Record<string, any>>(`/api/affectations/agent/${id}/active`);
      return response.data;
    } catch (error) {
      console.warn('[agentService] getAffectationActive failed:', error);
      return null;
    }
  },
};
