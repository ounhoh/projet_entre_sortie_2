package com.cese.process_entree_sortie.application.port.in.template.dependance;

import java.util.UUID;

public interface ValidateTemplateDependancesApi {
    Boolean ValidateTemplateDependances(UUID templateProcessusID);
}
