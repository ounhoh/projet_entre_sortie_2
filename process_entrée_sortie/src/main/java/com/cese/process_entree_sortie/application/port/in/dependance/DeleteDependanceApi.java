package com.cese.process_entree_sortie.application.port.in.dependance;

import java.util.UUID;

public interface DeleteDependanceApi {
    void deleteDependance(UUID dependanceId);
}
