package com.cese.process_entree_sortie.domain.utils.ValueObject;

public enum EtatAgent {
    entree(0),
    sortie (2),
    actif (1),
    mobiliteInterne (0),
    ancien (3);
    // 0 == Agent pas encore dans CESE ou en mobilité
    // 1 == Agent dans le cese ou en mobilité
    // 2 == Agent en sortie
    // 3 == Ancien agent
    EtatAgent(Integer valeur)
    {
        valeurAnciennete = valeur;
    }
    private final Integer valeurAnciennete;

    public Integer getValeurEtat(){
        return valeurAnciennete;
    }
    public static Boolean etatPouvantSuivre(EtatAgent premierEtat,EtatAgent autreEtat)
    {
        return (premierEtat.getValeurEtat() + 1) ==  autreEtat.getValeurEtat();
    }
}
