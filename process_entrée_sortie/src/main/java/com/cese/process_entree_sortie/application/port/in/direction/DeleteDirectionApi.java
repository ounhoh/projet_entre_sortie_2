package com.cese.process_entree_sortie.application.port.in.direction;

import java.util.UUID;

public interface DeleteDirectionApi {
    void deleteDirection(UUID directionId);
}
