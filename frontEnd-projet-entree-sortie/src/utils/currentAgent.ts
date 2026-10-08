// On réutilise la logique d'affichage de la direction (Prestataire, etc.)
import { getDirectionDisplay } from "./directionDisplay";

export type CurrentAgentInfo = {
  id?: string | null;
  nom?: string | null;
  prenom?: string | null;
  direction?: string | null;
  code?: string | null;
  role?: string | null;
};

const STORAGE_KEY = "currentAgent";

export function getCurrentAgent(): CurrentAgentInfo {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as CurrentAgentInfo;
    return parsed || {};
  } catch {
    return {};
  }
}

export function getCurrentAgentDisplay() {
  const agent = getCurrentAgent();
  const nom = (agent.nom || "").trim();
  const prenom = (agent.prenom || "").trim();
  const nomComplet = [prenom, nom].filter(Boolean).join(" ").trim();
  const displayedDirection = getDirectionDisplay({
    direction: agent.direction || undefined,
    role: agent.role || undefined,
  });
  return {
    nom: nomComplet || "Invité",
    direction: displayedDirection || "Non connecté",
    avatar: agent.code || "",
    id: agent.id || null,
  };
}
