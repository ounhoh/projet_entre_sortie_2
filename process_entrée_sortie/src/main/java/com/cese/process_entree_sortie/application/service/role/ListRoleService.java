package com.cese.process_entree_sortie.application.service.role;

import com.cese.process_entree_sortie.application.dto.role.sortie.RoleDTO;
import com.cese.process_entree_sortie.application.port.in.role.ListRoleApi;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.RoleEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListRoleService implements ListRoleApi {
    
    private final RoleJpaRepository roleJpaRepository;
    
    public ListRoleService(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }
    
    @Override
    public List<RoleDTO> getAllRoles() {
        List<RoleEntity> roles = roleJpaRepository.findAll();
        
        return roles.stream()
            .map(role -> new RoleDTO(
                role.getId(),
                role.getCode(),
                role.getLibelle(),
                role.getValeur()
            ))
            .collect(Collectors.toList());
    }
}
