package com.cese.process_entree_sortie.application.service.direction;

import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;
import com.cese.process_entree_sortie.application.port.in.direction.GetDirectionByIdApi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.utils.error.DirectionNonTrouveeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetDirectionByIdService implements GetDirectionByIdApi {
    
    private final DirectionSpi directionSpi;
    
    public GetDirectionByIdService(DirectionSpi directionSpi) {
        this.directionSpi = directionSpi;
    }
    
    @Override
    public DirectionDTO getDirectionById(UUID didrectionId) {
        Direction direction = directionSpi.findById(didrectionId)
            .orElseThrow(() -> new DirectionNonTrouveeException("Direction non trouvée avec l'ID: " + didrectionId));
        
        return new DirectionDTO(direction.id(), direction.codeDirection(), direction.libDirection());
    }
}