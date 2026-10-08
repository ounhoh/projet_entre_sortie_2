package com.cese.process_entree_sortie.domain.utils.error;

public class EmailInvalideException extends DomainException{
    public EmailInvalideException(String message)
    {
        super(message);
    }
}
