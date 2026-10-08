package com.cese.process_entree_sortie.application.port.in.role;

import com.cese.process_entree_sortie.application.dto.role.sortie.RoleDTO;

import java.util.List;

public interface ListRoleApi {
    List<RoleDTO> getAllRoles();
}
