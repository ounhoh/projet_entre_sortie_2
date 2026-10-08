// Utilitaires d'affichage pour la direction d'un agent
// Objectif : pour les prestataires créés via la DAPPI, afficher une étiquette claire
// au lieu de simplement "DAPPI".

type MaybeString = string | null | undefined;

export type AgentLike = {
  direction?: MaybeString;
  role?: MaybeString;
};

/** Détermine si un agent doit être considéré comme prestataire. */
export function isPrestataire(agent: AgentLike | null | undefined): boolean {
  if (!agent) return false;
  const role = String(agent.role || "").trim().toUpperCase();
  return role === "PRESTATAIRE";
}

/** Détermine si un agent doit être considéré comme conseiller. */
export function isConseiller(agent: AgentLike | null | undefined): boolean {
  if (!agent) return false;
  const role = String(agent.role || "").trim().toUpperCase();
  return role === "CONSEILLER";
}

/**
 * Retourne le libellé de direction à afficher pour un agent.
 *
 * Règle métier mise à jour :
 * - si l'agent est un prestataire (role = PRESTATAIRE) et que la direction réelle est DAPPI,
 *   on affiche "Prestataire".
 * - si l'agent est un conseiller (role = CONSEILLER) et que la direction réelle est DAF,
 *   on affiche "Conseiller/Attaché".
 * - dans tous les autres cas, on affiche la direction telle que renvoyée par le backend.
 */
export function getDirectionDisplay(agent: AgentLike | null | undefined): string {
  if (!agent) return "Non défini(e)";

  const rawDirection = String(agent.direction || "").trim();

  if (isPrestataire(agent)) {
    // Demande métier : toujours afficher "Prestataire" pour un agent dont le rôle est PRESTATAIRE,
    // quelle que soit la direction réelle (DAPPI/DSIUN/DAF/etc.).
    return "Prestataire";
  }

  if (isConseiller(agent)) {
    // Demande métier : toujours afficher "Conseiller/Attaché" pour un agent dont le rôle est CONSEILLER,
    // quelle que soit la direction réelle (DAF/DSIUN/DAPPI/etc.).
    return "Conseiller/Attaché";
  }

  return rawDirection || "Non défini(e)";
}

