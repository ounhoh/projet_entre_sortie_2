package com.cese.process_entree_sortie.application.dto.template.entree.tache;
import java.util.UUID;

public record UpdateTemplateTacheCommand(UUID tacheId,String libelle,String description, int delaiJour) {

}
