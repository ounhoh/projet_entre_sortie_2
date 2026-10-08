import apiClient from '@/lib/axios';

export type StatutProcessusDTO = {
  id: string;
  code: string;
  libelle: string;
};

export type TemplateGroupeTacheDTO = {
  id: string;
  code: string;
  libelle: string;
  statutProcessusId: string;
  codeDirection?: string;
  libelleDirection?: string;
  isDirectionConcernee?: boolean;
  tacheDTOList: any[]; // TemplateTacheDTO[]
};

export type TemplateProcessusDTO = {
  id: string;
  code: string;
  libelle: string;
  statutProcessusDTOList: StatutProcessusDTO[];
  groupeTacheDTOS: TemplateGroupeTacheDTO[];
};

export const templateProcessusService = {
  async getAllTemplates(type?: string): Promise<TemplateProcessusDTO[]> {
    const params: Record<string, string> = {};
    if (type) {
      params.type = type;
    }
    const response = await apiClient.get<TemplateProcessusDTO[]>('/api/templates/processus', {
      params
    });
    return response.data;
  },

  async getTemplateById(id: string): Promise<TemplateProcessusDTO> {
    const response = await apiClient.get<TemplateProcessusDTO>(`/api/templates/processus/${id}`);
    return response.data;
  },

  async getTemplatesByType(type: string): Promise<TemplateProcessusDTO[]> {
    const response = await apiClient.get<TemplateProcessusDTO[]>(`/api/templates/processus/type/${type}`);
    return response.data;
  },

  async getTemplateByCode(code: string): Promise<TemplateProcessusDTO | null> {
    // Récupérer tous les templates et trouver celui avec le code correspondant
    const response = await apiClient.get<TemplateProcessusDTO[]>('/api/templates/processus');
    return response.data.find(t => t.code === code) || null;
  },
};
