import type { AgentDTO } from '@/service/processusService';
import type { ListeAgent } from '@/components/tableau/tableau-Agents/Column';
import { getDirectionDisplay } from './directionDisplay';

// Fonction pour mapper AgentDTO vers ListeAgent
export function mapAgentToListeAgent(agent: AgentDTO): ListeAgent {
  // Formater le nom complet
  const nomPrenom = `${agent.prenom} ${agent.nom}`;

  // Formater la date de sortie
  const formatDate = (dateString: string | null) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', { 
        day: '2-digit', 
        month: '2-digit', 
        year: '2-digit' 
      });
    } catch {
      return dateString;
    }
  };

  return {
    id: agent.id,
    agent: {
      nomPrenom: nomPrenom,
      // Pour les prestataires et conseillers/attachés DAF, afficher un libellé métier
      // (\"Prestataire\" ou \"Conseiller/Attaché\") plutôt que simplement \"DAPPI\" ou \"DAF\"
      direction: getDirectionDisplay({
        direction: agent.direction || agent.libelle,
        role: agent.role,
      }),
    },
    dateSortie: formatDate(agent.dateSortie),
    etatAgent: agent.etatAgent,
    role: agent.role,
  };
}

// Fonction pour filtrer les agents actifs (ceux avec état 'actif')
export function filterActiveAgents(agents: ListeAgent[]): ListeAgent[] {
  return agents.filter(agent => {
    // Vérifier l'état de l'agent - seuls les agents avec état 'actif' sont considérés comme actifs
    if (agent.etatAgent) {
      return agent.etatAgent === 'actif';
    }
    // Fallback pour compatibilité: si pas d'état défini, vérifier la date de sortie
    // Si pas de date de sortie, l'agent est actif
    if (!agent.dateSortie || agent.dateSortie === '') {
      return true;
    }
    // Si date de sortie dans le futur, l'agent est actif
    try {
      // Parser la date au format DD/MM/YY
      const [day, month, year] = agent.dateSortie.split('/');
      const fullYear = 2000 + parseInt(year);
      const dateSortie = new Date(fullYear, parseInt(month) - 1, parseInt(day));
      const aujourdhui = new Date();
      aujourdhui.setHours(0, 0, 0, 0); // Réinitialiser l'heure pour la comparaison
      dateSortie.setHours(0, 0, 0, 0);
      return dateSortie > aujourdhui || dateSortie.getTime() === aujourdhui.getTime();
    } catch {
      // Si erreur de parsing, considérer comme actif
      return true;
    }
  });
}

// Fonction pour filtrer les agents inactifs (ceux avec date de sortie passée)
export function filterInactiveAgents(agents: ListeAgent[]): ListeAgent[] {
  return agents.filter(agent => {
    // Si pas de date de sortie, l'agent est actif (donc pas inactif)
    if (!agent.dateSortie || agent.dateSortie === '') {
      return false;
    }
    // Si date de sortie dans le passé, l'agent est inactif
    try {
      // Parser la date au format DD/MM/YY
      const [day, month, year] = agent.dateSortie.split('/');
      const fullYear = 2000 + parseInt(year);
      const dateSortie = new Date(fullYear, parseInt(month) - 1, parseInt(day));
      const aujourdhui = new Date();
      aujourdhui.setHours(0, 0, 0, 0); // Réinitialiser l'heure pour la comparaison
      dateSortie.setHours(0, 0, 0, 0);
      return dateSortie < aujourdhui;
    } catch {
      // Si erreur de parsing, considérer comme actif
      return false;
    }
  });
}

// Fonction pour filtrer les anciens agents (ceux avec état 'ancien')
export function filterAncienAgents(agents: ListeAgent[]): ListeAgent[] {
  return agents.filter(agent => {
    // Vérifier l'état de l'agent - seuls les agents avec état 'ancien' sont considérés comme anciens
    if (agent.etatAgent) {
      return agent.etatAgent === 'ancien';
    }
    // Si pas d'état défini, ne pas inclure dans les anciens agents
    return false;
  });
}
