package com.cese.process_entree_sortie.application.port.out.direction;

import com.cese.process_entree_sortie.domain.Direction.model.Direction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectionSpi {
    Direction save(Direction direction);
    Optional<Direction> findById(UUID directionId);
    List<Direction> findAll();
    void delete(Direction direction);
}
