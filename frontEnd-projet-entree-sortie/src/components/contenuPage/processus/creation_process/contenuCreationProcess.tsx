import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { Retour } from "../../../utils/Retour";
import { templateProcessusService, type TemplateProcessusDTO } from "@/service/templateProcessusService";
import { GroupeTacheView } from "../contenu_process/GroupeTacheView";
import { ProcessStatusStepper } from "../ProcessStatusStepper";
import { agentService } from "@/service/agentService";

export function ContenuCreationProcessPage() {
    const { templateId } = useParams<{ templateId: string }>();
    const location = useLocation();
    const [template, setTemplate] = useState<TemplateProcessusDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [prefill, setPrefill] = useState<Record<string, any> | null>(null);
    const [prefillAgentId, setPrefillAgentId] = useState<string | null>(null);

    useEffect(() => {
        async function fetchTemplate() {
            if (!templateId) {
                setError("ID de template manquant");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);
                const templateData = await templateProcessusService.getTemplateById(templateId);
                setTemplate(templateData);
            } catch (err) {
                console.error('Erreur lors du chargement du template:', err);
                setError('Erreur lors du chargement du template. Veuillez réessayer.');
            } finally {
                setLoading(false);
            }
        }

        fetchTemplate();
    }, [templateId]);

    useEffect(() => {
        const state = location.state as { agentId?: string; prefill?: Record<string, any> } | null;
        if (state?.agentId) {
            setPrefillAgentId(state.agentId);
        }
        if (state?.prefill) {
            setPrefill(state.prefill);
        }
    }, [location.state]);

    const searchParams = new URLSearchParams(location.search);
    const modePrestataire = searchParams.get('mode') === 'prestataire';
    const modeConseiller = searchParams.get('mode') === 'conseiller';

    useEffect(() => {
        if (!prefillAgentId || prefill) return;
        agentService.getAgentById(prefillAgentId)
            .then(agent => {
                setPrefill({
                    nom_agent: agent.nom,
                    prenom_agent: agent.prenom,
                    email_agent: agent.email,
                    date_depart: agent.dateSortie || "",
                });
            })
            .catch(() => {
                setPrefill({});
            });
    }, [prefillAgentId, prefill]);

    // Récupérer l'ID du premier statut (statut actuel)
    // Doit être appelé avant les returns conditionnels pour respecter les règles des Hooks
    const premierStatutId = template?.statutProcessusDTOList?.[0]?.id;

    if (loading) {
        return (
            <div className="p-8">
                <div className="flex items-center justify-center p-8">
                    <p>Chargement du template...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-8">
                <Retour />
                <div className="flex items-center justify-center p-8 text-red-500">
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    if (!template) {
        return (
            <div className="p-8">
                <Retour />
                <div className="flex items-center justify-center p-8">
                    <p>Template non trouvé</p>
                </div>
            </div>
        );
    }

    return (
        <div className="flex-1 min-h-0 flex flex-col">
            <div className="p-4 border-b flex  justify-between">
                <Retour />
                {template.statutProcessusDTOList && template.statutProcessusDTOList.length > 0 && (
                    <div className="flex-1">
                        <ProcessStatusStepper statuts={template.statutProcessusDTOList.filter((statut: { code: string }) => statut.code !== "archive")} />
                    </div>
                )}
            </div>
            <div className="flex-1 min-h-0">
                <GroupeTacheView
                    groupes={template.groupeTacheDTOS || []}
                    templateLibelle={template.libelle}
                    premierStatutId={premierStatutId}
                    templateId={templateId}
                    template={template}
                    prefillValues={prefill || undefined}
                    prefillAgentId={prefillAgentId || undefined}
                    modePrestataire={modePrestataire}
                    modeConseiller={modeConseiller}
                />
            </div>
        </div>
    );
}
