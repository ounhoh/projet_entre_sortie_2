import apiClient from '@/lib/axios';

export type DirectionDTO = {
  id: string;
  code: string;
  libelle: string;
};

export const directionService = {
  async getDirections(): Promise<DirectionDTO[]> {
    const response = await apiClient.get<DirectionDTO[]>('/api/directions');
    return response.data;
  },
};
