package com.cese.process_entree_sortie.domain.utils.error;

public abstract  class DomainException extends  RuntimeException {
    public  DomainException(String message)
    {
        super(message);
    }
}
