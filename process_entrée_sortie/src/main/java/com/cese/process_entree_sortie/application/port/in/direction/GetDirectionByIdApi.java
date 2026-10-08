package com.cese.process_entree_sortie.application.port.in.direction;

import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;

import java.util.UUID;

public interface GetDirectionByIdApi {
    DirectionDTO getDirectionById(UUID didrectionId);
}
