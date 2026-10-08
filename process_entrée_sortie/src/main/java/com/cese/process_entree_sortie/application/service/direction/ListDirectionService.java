package com.cese.process_entree_sortie.application.service.direction;

import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;
import com.cese.process_entree_sortie.application.port.in.direction.ListDirectionApi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListDirectionService implements ListDirectionApi {
    
    private final DirectionSpi directionSpi;
    
    public ListDirectionService(DirectionSpi directionSpi) {
        this.directionSpi = directionSpi;
    }
    
    @Override
    public List<DirectionDTO> getListDirection() {
        List<Direction> directions = directionSpi.findAll();
        
        return directions.stream()
            .map(dir -> new DirectionDTO(dir.id(), dir.codeDirection(), dir.libDirection()))
            .collect(Collectors.toList());
    }
}