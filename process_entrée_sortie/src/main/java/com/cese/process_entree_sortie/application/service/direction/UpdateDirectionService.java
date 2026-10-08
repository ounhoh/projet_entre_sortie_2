package com.cese.process_entree_sortie.application.service.direction;

import com.cese.process_entree_sortie.application.dto.direction.entree.UpdateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;
import com.cese.process_entree_sortie.application.port.in.direction.UpdateDirectionApi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.utils.error.DirectionNonTrouveeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateDirectionService implements UpdateDirectionApi {
    
    private final DirectionSpi directionSpi;
    
    public UpdateDirectionService(DirectionSpi directionSpi) {
        this.directionSpi = directionSpi;
    }
    
    @Override
    public DirectionDTO updateDirection(UpdateDirectionCommand command) {
        directionSpi.findById(command.id())
            .orElseThrow(() -> new DirectionNonTrouveeException("Direction non trouvée avec l'ID: " + command.id()));
        
        Direction directionModifiee = new Direction(
            command.id(),
            command.code(),
            command.libelle()
        );
        
        Direction directionSauvegardee = directionSpi.save(directionModifiee);
        
        return new DirectionDTO(
            directionSauvegardee.id(),
            directionSauvegardee.codeDirection(),
            directionSauvegardee.libDirection()
        );
    }
}