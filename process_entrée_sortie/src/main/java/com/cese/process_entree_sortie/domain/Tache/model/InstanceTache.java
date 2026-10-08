package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import com.cese.process_entree_sortie.domain.utils.error.DateEcheanceInvalideException;
import com.cese.process_entree_sortie.domain.utils.error.StatutTacheInvalideException;

import java.util.*;
import java.time.LocalDate;

public record InstanceTache (UUID id, String code,String libelle, TacheContenu contenu, StatutTache statut,
                             LocalDate dateEcheance, UUID templateId, UUID groupeTacheId, UUID dependanceId)
    implements NoeudTache{
    public InstanceTache {
        Objects.requireNonNull(id);
        Objects.requireNonNull(code);
        Objects.requireNonNull(contenu);
        Objects.requireNonNull(statut);
        Objects.requireNonNull(dateEcheance);
        Objects.requireNonNull(templateId);
        Objects.requireNonNull(groupeTacheId);
        // dependanceId peut être null (tâche sans dépendance)
    }


    public InstanceTache(UUID id, String code, String libelle,
                         TacheContenu contenu, LocalDate dateEcheance, UUID templateId,
                         UUID groupeTacheId)
    {
        this(id,code,libelle, contenu, StatutTache.aFaire, dateEcheance, templateId,groupeTacheId, null);
    }

    public InstanceTache changerDateEcheance(LocalDate newDateEcheance) // changer la date d'echeance
    {
        if (LocalDate.now().isAfter(newDateEcheance))
        {
            throw new DateEcheanceInvalideException("la date d'échéance est avant la date actuel");
        }
        return new InstanceTache(id,code,libelle,contenu,statut,newDateEcheance,templateId,groupeTacheId,dependanceId);
    }

    public InstanceTache activerTache() // active la tache
    {
        if (!StatutTache.aFaire.equals(statut))
        {
            throw new StatutTacheInvalideException("Statut de la tâche qui n'est pas au statut: a Faire");
        }
        return new InstanceTache(id,code,libelle,contenu,StatutTache.enCours,dateEcheance,templateId,groupeTacheId,dependanceId);
    }
    public InstanceTache mettreAFaire() // mettre la tache à l'état : à faire
    {

        return new InstanceTache(id,code,libelle,contenu,StatutTache.aFaire,dateEcheance,templateId,groupeTacheId,dependanceId);
    }
    public InstanceTache demarer() // démarer une tache
    {
        if (!StatutTache.aFaire.equals(statut))
        {
            throw new StatutTacheInvalideException("Statut de la tâche qui n'est pas au statut: A FAIRE");
        }
        return new InstanceTache(id,code,libelle,contenu,StatutTache.enCours,dateEcheance,templateId,groupeTacheId,dependanceId);

    }

    public InstanceTache reouvertureTache() // passe un statut de fait à en cours
    {
        if (!statut.equals(StatutTache.fait))
        {
            throw new StatutTacheInvalideException("Statut  de la tache qui n'est pas au statut: Fait");
        }
        return new InstanceTache(id,code,libelle,contenu,StatutTache.enCours,dateEcheance,templateId,groupeTacheId,dependanceId);
    }

    public InstanceTache terminer() // pour terminer la tâche
    {
        if(!StatutTache.enCours.equals(statut))
        {
            throw new StatutTacheInvalideException("Statut de la tâche qui n'est pas au statut: en Cours");
        }
        return new InstanceTache(id, code,libelle, contenu,StatutTache.fait, dateEcheance,templateId,groupeTacheId,dependanceId);
    }

    public boolean estEnRetard() // vérfier si la tache est en retards
    {
        return LocalDate.now().isAfter(dateEcheance) && !StatutTache.fait.equals(statut);
    }


    @Override
    public TacheType type() {
        return TacheType.tache;
    }

    @Override
    public Optional<InstanceTache> getTache() {
        return Optional.of(this);
    }

    @Override
    public Optional<InstanceGroupeTache> getGroupeTache() {
        return Optional.empty();
    }

}
