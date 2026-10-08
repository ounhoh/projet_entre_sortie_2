package com.cese.process_entree_sortie.application.port.in.direction;

import com.cese.process_entree_sortie.application.dto.direction.entree.CreateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;

public interface createDirectionApi {
    DirectionDTO createDirection(CreateDirectionCommand command);
}
