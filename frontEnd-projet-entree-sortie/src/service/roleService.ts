import apiClient from '@/lib/axios';

export type RoleDTO = {
  id: string;
  code: string;
  libelle: string;
  valeur: number;
};

export const roleService = {
  async getAllRoles(): Promise<RoleDTO[]> {
    const response = await apiClient.get<RoleDTO[]>('/api/roles');
    return response.data;
  },
};
