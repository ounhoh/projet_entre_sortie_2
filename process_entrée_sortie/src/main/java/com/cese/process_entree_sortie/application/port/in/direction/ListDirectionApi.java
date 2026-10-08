package com.cese.process_entree_sortie.application.port.in.direction;

import com.cese.process_entree_sortie.application.dto.direction.sortie.DirectionDTO;

import java.util.List;

public interface ListDirectionApi {
    List<DirectionDTO> getListDirection();
}
