package com.cese.process_entree_sortie.application.service.direction;

import com.cese.process_entree_sortie.application.dto.direction.entree.CreateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;
import com.cese.process_entree_sortie.application.port.in.direction.createDirectionApi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateDirectionService implements createDirectionApi {
    
    private final DirectionSpi directionSpi;
    
    public CreateDirectionService(DirectionSpi directionSpi) {
        this.directionSpi = directionSpi;
    }
    
    @Override
    public DirectionDTO createDirection(CreateDirectionCommand command) {
        Direction direction = new Direction(
            UUID.randomUUID(),
            command.code(),
            command.libelle()
        );
        
        Direction directionSauvegardee = directionSpi.save(direction);
        
        return new DirectionDTO(
            directionSauvegardee.id(),
            directionSauvegardee.codeDirection(),
            directionSauvegardee.libDirection()
        );
    }
}