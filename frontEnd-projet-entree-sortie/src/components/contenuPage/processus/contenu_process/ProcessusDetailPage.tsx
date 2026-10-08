import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { processusService } from "@/service/processusService";
import type { ProcessDetailDTO } from "@/service/processusService";
import { templateProcessusService } from "@/service/templateProcessusService";
import { Retour } from "../../../utils/Retour";
import { InstanceGroupeTacheView } from "./InstanceGroupeTacheView";
import { ProcessStatusStepper } from "../ProcessStatusStepper";

type LoadState = "idle" | "loading" | "error";

export default function ProcessusDetailPage() {
  const { processusId } = useParams<{ processusId: string }>();
  const [state, setState] = useState<LoadState>("idle");
  const [error, setError] = useState<string | null>(null);
  const [details, setDetails] = useState<ProcessDetailDTO | null>(null);
  const [template, setTemplate] = useState<any>(null);

  useEffect(() => {
    const load = async () => {
      if (!processusId) return;
      setState("loading");
      setError(null);
      try {
        const resp = await processusService.getProcessusDetails(processusId);
        setDetails(resp);
        
        // Récupérer le template pour avoir :
        // 1. La liste des statuts pour le ProcessStatusStepper
        // 2. Les templateGroupes pour filtrer les groupes par statut (via statutProcessusId)
        if (resp.processInfo.templateId) {
          try {
            const templateData = await templateProcessusService.getTemplateById(resp.processInfo.templateId);
            setTemplate(templateData);
          } catch (templateError) {
            console.error('Erreur lors du chargement du template:', templateError);
            // Ne pas bloquer l'affichage si le template ne peut pas être chargé
          }
        }
        
        setState("idle");
      } catch (e: any) {
        setError(e?.message || "Erreur lors du chargement");
        setState("error");
      }
    };
    load();
  }, [processusId]);

  if (state === "loading") {
    return (
      <div className="flex-1 min-h-0 flex flex-col">
        <div className="p-8">
          <div className="flex items-center justify-center p-8">
            <p>Chargement du processus...</p>
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

  if (!details) {
    return (
      <div className="flex-1 min-h-0 flex flex-col">
        <div className="p-8">
          <Retour />
          <div className="flex items-center justify-center p-8">
            <p>Processus non trouvé</p>
          </div>
        </div>
      </div>
    );
  }

  const statutActuelId = details.processInfo.statut.id;
  const statutsSansArchive = template?.statutProcessusDTOList
    ? template.statutProcessusDTOList.filter((statut: { code: string }) => statut.code !== "archive")
    : [];
  const agent = details.processInfo.agent;
  const processusLibelle = agent ?  `${agent.prenom} ${agent.nom}` // nom complet de l'agent
  : (details.processInfo.code || `Processus ${details.processInfo.id.substring(0, 8)}`);

  return (
    <div className="flex-1 min-h-0 flex flex-col">
      <div className="p-4 border-b flex justify-between">
        <Retour />
        {statutsSansArchive.length > 0 && (
          <div className="flex-1">
            <ProcessStatusStepper 
              statuts={statutsSansArchive} 
              currentStatut={statutActuelId}
            />
          </div>
        )}
      </div>
      <div className="flex-1 min-h-0">
        <InstanceGroupeTacheView
          groupes={details.groupeTacheList || []}
          templateGroupes={template?.groupeTacheDTOS || []}
          processusLibelle={processusLibelle}
          statutActuelId={statutActuelId}
          direction={details.processInfo.labelleDirectionConcernee}
          statutsProcessus={statutsSansArchive}
          agentId={agent?.id}
          agentRole={agent?.role}
          processusId={processusId}
          dependanceList={details.dependanceList || []}
          onDataUpdate={(updatedDetails) => {
            setDetails(updatedDetails);
            // Recharger le template aussi si nécessaire
            if (updatedDetails.processInfo.templateId) {
              templateProcessusService.getTemplateById(updatedDetails.processInfo.templateId)
                .then(setTemplate)
                .catch(err => console.error('Erreur lors du rechargement du template:', err));
            }
          }}
        />
      </div>
    </div>
  );
}
