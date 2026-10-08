import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { agentService } from "@/service/agentService";
import { templateProcessusService } from "@/service/templateProcessusService";
import { Retour } from "../../utils/Retour";
import { InputGroup, InputGroupInput, InputGroupAddon } from "../../ui/input-group";
import { Label } from "../../ui/label";
import { Calendar, User, Building2, Mail, Briefcase } from "lucide-react";
import type { AgentDTO } from "@/service/processusService";
import { getDirectionDisplay } from "@/utils/directionDisplay";
import { Button } from "../../ui/button";
import { Plus } from "lucide-react";
import { getCurrentAgent } from "@/utils/currentAgent";
import { isPrestataire } from "@/utils/directionDisplay";
import { isConseiller } from "@/utils/directionDisplay";
type LoadState = "idle" | "loading" | "error";

// Fonction pour obtenir l'icône depuis le nom
function getIcon(iconName?: string) {
  const icons: Record<string, React.ReactNode> = {
    calendar: <Calendar className="size-4" />,
    user: <User className="size-4" />,
    building: <Building2 className="size-4" />,
    mail: <Mail className="size-4" />,
    briefcase: <Briefcase className="size-4" />,
  };
  return icons[iconName || ''] || null;
}

// Fonction pour formater une valeur
function formatValue(value: any, type?: 'text' | 'date' | 'email'): string {
  if (value === null || value === undefined || value === '') {
    return 'Non défini(e)';
  }

  if (type === 'date') {
    try {
      const date = new Date(value);
      if (isNaN(date.getTime())) {
        return 'Non défini(e)';
      }
      return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch {
      return 'Non défini(e)';
    }
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return 'Non défini(e)';
    }
    // Formater les éléments du tableau
    const formatted = value.map(item => {
      if (typeof item === 'object' && item !== null) {
        return JSON.stringify(item);
      }
      return String(item);
    });
    return formatted.join(', ');
  }

  if (typeof value === 'object' && value !== null) {
    // Si c'est un objet, essayer de le convertir en string intelligible
    try {
      return JSON.stringify(value);
    } catch {
      return 'Non défini(e)';
    }
  }

  const stringValue = String(value);
  return stringValue.trim() === '' ? 'Non défini(e)' : stringValue;
}

type ChampAffichage = {
  label: string;
  value: string | null | undefined;
  type?: 'text' | 'date' | 'email';
  icon?: string;
};

export default function AgentDetailPage() {
  const { agentId } = useParams<{ agentId: string }>();
  const navigate = useNavigate();
  const [state, setState] = useState<LoadState>("idle");
  const [error, setError] = useState<string | null>(null);
  const [agent, setAgent] = useState<AgentDTO | null>(null);
  const [sortieLoading, setSortieLoading] = useState(false);
  const [sortieError, setSortieError] = useState<string | null>(null);
  const champsAgent = useMemo<ChampAffichage[]>(() => {
    if (!agent) return [];
    return [
      { label: "Nom", value: agent.nom, icon: "user" },
      { label: "Prénom", value: agent.prenom, icon: "user" },
      { label: "Email", value: agent.email, type: "email", icon: "mail" },
      { label: "Rôle", value: agent.role, icon: "briefcase" },
      {
        label: "Direction",
        // Pour les prestataires DAPPI ou conseillers/attachés DAF, afficher un libellé explicite
        value: getDirectionDisplay({
          direction: agent.direction,
          role: agent.role,
        }),
        icon: "building",
      },
      { label: "Date d'arrivée", value: agent.dateArrivee ?? null, type: "date", icon: "calendar" },
      { label: "Date de sortie", value: agent.dateSortie ?? null, type: "date", icon: "calendar" },
      { label: "Clé du bureau", value: agent.numeroBureau ?? null, icon: "building" },
    ];
  }, [agent]);

  useEffect(() => {
    const load = async () => {
      if (!agentId) return;
      setState("loading");
      setError(null);
      try {
        // Charger l'agent
        const agentData = await agentService.getAgentById(agentId);
        setAgent(agentData);

        setState("idle");
      } catch (e: any) {
        console.error('Erreur lors du chargement:', e);
        setError(e?.message || "Erreur lors du chargement");
        setState("error");
      }
    };
    load();
  }, [agentId]);

  if (state === "loading") {
    return (
      <div className="flex-1 min-h-0 flex flex-col">
        <div className="p-8">
          <div className="flex items-center justify-center p-8">
            <p>Chargement des informations de l'agent...</p>
          </div>
        </div>
      </div>
    );
  }

  if (state === "error") {
    return (
      <div className="flex-1 min-h-0 flex flex-col">
        <div className="p-8">
          <Retour />
          <div className="flex items-center justify-center p-8 text-red-500">
            <p>{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!agent) {
    return (
      <div className="flex-1 min-h-0 flex flex-col">
        <div className="p-8">
          <Retour />
          <div className="flex items-center justify-center p-8">
            <p>Agent non trouvé</p>
          </div>
        </div>
      </div>
    );
  }

  const agentLibelle = `${agent.prenom} ${agent.nom}`;
  const isAncien = String(agent.etatAgent || '').trim().toLowerCase() === 'ancien';
  const currentAgent = getCurrentAgent();
  const isDrh = (currentAgent.direction || "").trim().toLowerCase() === "drh";
  const isDappi = (() => {
    const dir = String(currentAgent.direction || "").trim().toLowerCase();
    return dir === "dappi" || dir === "direction_appi" || dir === "d.a.p.p.i";
  })();
  const isDsiun = (() => {
    const dir = String(currentAgent.direction || "").trim().toLowerCase();
    return dir === "dsiun" || dir === "direction_siun" || dir === "d.s.i.u.n";
  })();
  const isDaf = (() => {
    const dir = String(currentAgent.direction || "").trim().toLowerCase();
    return dir === "daf" || dir === "direction_af" || dir === "d.a.f";
  })();
  const isSg = (() => {
    const dir = String(currentAgent.direction || "").trim().toLowerCase();
    return dir === "sg" || dir === "direction_sg";
  })();
  const isDappiLike = isDappi; // alias lisible
  const isDsiunLike = isDsiun; // alias lisible
  const isDafLike = isDaf; // alias lisible
  const isSgLike = isSg; // alias lisible

  // Déterminer le type d'agent (prestataire, conseiller, ou classique)
  const isPrestataireCible = isPrestataire({ role: agent.role, direction: agent.direction });
  const isConseillerCible = isConseiller({ role: agent.role, direction: agent.direction });
  
  // Déterminer si l'utilisateur peut lancer la sortie
  const canLaunchSortie = !isAncien && (
    (isPrestataireCible && (isDappi || isDsiun || isDaf)) ||
    (isConseillerCible && (isDafLike || isDsiunLike || isSgLike)) ||
    (!isPrestataireCible && !isConseillerCible && isDrh)
  );
  
  const handleLancerSortie = async () => {
    if (!agent) return;
    // Vérifier les droits selon le type d'agent
    if (isPrestataireCible && !(isDappi ||isDsiun || isDaf)) return;
    if (isConseillerCible && !(isDafLike || isDsiunLike || isSgLike)) return;
    if (!isPrestataireCible && !isConseillerCible && !isDrh) return;
    
    setSortieLoading(true);
    setSortieError(null);
    try {
      const templateSortie = await templateProcessusService.getTemplateByCode("sortie_agent_creation");
      let templateToUse = templateSortie;
      if (!templateToUse) {
        const templates = await templateProcessusService.getTemplatesByType("sortie");
        templateToUse = templates.find(t => t.code.includes("sortie")) || null;
      }
      if (!templateToUse) {
        throw new Error("Template de sortie non trouvé");
      }
      const prefill = {
        nom_agent: agent.nom,
        prenom_agent: agent.prenom,
        email_agent: agent.email,
        date_depart: agent.dateSortie || "",
      };
      
      // Déterminer le mode à utiliser en fonction du type d'agent
      let search = "";
      if (isPrestataireCible) {
        search = "?mode=prestataire";
      } else if (isConseillerCible) {
        search = "?mode=conseiller";
      }
      
      navigate(`/creation_process/${templateToUse.id}${search}`, {
        state: {
          agentId: agent.id,
          prefill,
        },
      });
    } catch (err: any) {
      console.error("Erreur lors du lancement du processus de sortie:", err);
      setSortieError(err?.message || "Impossible de lancer le processus de sortie");
    } finally {
      setSortieLoading(false);
    }
  };

  return (
    <div className="flex-1 min-h-0 flex flex-col">
      <div className="p-4 border-b flex justify-between items-center">
        <Retour />
        <h1 className="text-2xl font-bold">{agentLibelle}</h1>
        <div></div>
      </div>

      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto space-y-8">
          <div className="bg-card text-card-foreground rounded-xl border p-6 space-y-4">
            <h2 className="text-xl font-semibold mb-4">Informations de l'utilisateur</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {champsAgent.map((champ) => (
                <div key={champ.label} className="flex flex-col gap-2">
                  <Label>{champ.label}</Label>
                  <InputGroup>
                    {champ.icon && (
                      <InputGroupAddon align="inline-start">
                        {getIcon(champ.icon)}
                      </InputGroupAddon>
                    )}
                    <InputGroupInput
                      type="text"
                      value={formatValue(champ.value, champ.type)}
                      readOnly
                      className="bg-muted/50 cursor-default"
                    />
                  </InputGroup>
                </div>
              ))}
            </div>
          </div>
          {canLaunchSortie && (
            <div className="flex items-center justify-end gap-3">
              {sortieError && (
                <span className="text-sm text-red-500">{sortieError}</span>
              )}
              <Button
                variant="default"
                size="sm"
                className="rounded-lg w-fit"
                onClick={handleLancerSortie}
                disabled={sortieLoading}
              >
                <Plus className="size-4" />
                {sortieLoading ? "Lancement..." : "Lancer le processus de sortie"}
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
