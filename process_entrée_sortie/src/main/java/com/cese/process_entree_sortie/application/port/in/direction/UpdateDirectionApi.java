package com.cese.process_entree_sortie.application.port.in.direction;


import com.cese.process_entree_sortie.application.dto.direction.entree.UpdateDirectionCommand;
import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;

public interface UpdateDirectionApi {
    DirectionDTO updateDirection(UpdateDirectionCommand command);
}
