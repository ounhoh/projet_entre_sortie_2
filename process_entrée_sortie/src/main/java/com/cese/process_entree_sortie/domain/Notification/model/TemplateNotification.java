package com.cese.process_entree_sortie.domain.Notification.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record TemplateNotification(UUID id, NiveauNotification niveau,
                                   String codeNotification, String object, String description,
                                   TacheType typeEnvoyeur, UUID tacheId, UUID groupeTacheId) {
    public TemplateNotification {
        Objects.requireNonNull(niveau);
        Objects.requireNonNull(typeEnvoyeur);
        Objects.requireNonNull(codeNotification);
        Objects.requireNonNull(object);
        Objects.requireNonNull(description);
    }


    public Optional<UUID> getOptionalTache()
    {
        return Optional.ofNullable(tacheId);
    }

    public Optional<UUID> getOptionalGroupeTache()
    {
        return Optional.ofNullable(groupeTacheId);
    }
}
