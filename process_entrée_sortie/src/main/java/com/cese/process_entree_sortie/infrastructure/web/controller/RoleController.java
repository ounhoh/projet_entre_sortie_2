package com.cese.process_entree_sortie.infrastructure.web.controller;

import com.cese.process_entree_sortie.application.dto.role.sortie.RoleDTO;
import com.cese.process_entree_sortie.application.port.in.role.ListRoleApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    
    private final ListRoleApi listRoleApi;
    
    public RoleController(ListRoleApi listRoleApi) {
        this.listRoleApi = listRoleApi;
    }
    
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = listRoleApi.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
