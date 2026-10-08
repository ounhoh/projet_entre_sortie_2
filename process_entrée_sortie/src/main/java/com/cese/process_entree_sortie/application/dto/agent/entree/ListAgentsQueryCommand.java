package com.cese.process_entree_sortie.application.dto.agent.entree;

public record ListAgentsQueryCommand(int page, int size, String sortBy, String direction) {
    public ListAgentsQueryCommand {
     if (page < 0)
     {
         page = 0;
     }
     if (size <= 0 || size > 100)
     {
         size = 20;
     }

     if (sortBy == null || sortBy.isEmpty())
     {
         sortBy = "nom";
     }
     // Mapper "date" vers "nom" pour compatibilité (le champ "date" n'existe pas)
     if ("date".equalsIgnoreCase(sortBy))
     {
         sortBy = "nom";
     }
     if (direction == null || direction.isEmpty())
     {
         direction = "ASC";
     }
    }
}
