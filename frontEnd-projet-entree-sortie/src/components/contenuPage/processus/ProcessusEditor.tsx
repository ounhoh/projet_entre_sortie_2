import { templateProcessusService } from "@/service/templateProcessusService";
import type { TemplateProcessusDTO } from "@/service/templateProcessusService";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card";
import { Input } from "../../ui/input";
import { AnimatedList } from "../../ui/animated-list";
import { Button } from "../../ui/button";
import { CheckCircle, NotebookText, Pencil } from "lucide-react";
import { getIconEntree, getIconMobiliteInterne, getIconSortie } from "../../iconTypeProcessus/iconTypeProcessus";
import type { JSX } from "react";
const affichageTemplateProcessus = (template: TemplateProcessusDTO) => {
    return (
        <div>
            <h2 className="font-bold">{template.libelle}</h2>
            <div className="flex items-center justify-center gap-2">
            <NotebookText className="w-4 h-4"/>
            <h2>{template.groupeTacheDTOS.length ? template.groupeTacheDTOS.length + " groupes de tâches" : "Aucun groupe de tâches"}</h2>
            </div>
            <div className="flex items-center justify-center gap-2">
            <CheckCircle className="w-4 h-4"/>
            
            <h2>{template.statutProcessusDTOList.length ? template.statutProcessusDTOList.length + " statuts" : "Aucun statut"}</h2>
            </div>
        </div>
    )
}

const buttonTemplateProcessus = (template: TemplateProcessusDTO, navigate: (path: string) => void, icon: JSX.Element) => {
    return (
        <Button
            key={template.id}
            variant="outline"
            className="inline-flex w-fit h-auto flex-col gap-2 items-center justify-start py-3 text-center"
            onClick={() => navigate(`/creation_process/${template.id}`)}
        >
            <div className="flex w-full items-center justify-center">
                {icon}
            </div>
            {affichageTemplateProcessus(template)}
        </Button>
    )
}

const listeTemplateProcessus = (templates: TemplateProcessusDTO[], navigate: (path: string) => void, icon: JSX.Element) => {
    return (
        <AnimatedList>
        <div className="flex flex-col gap-4 items-start">
            {templates.map((template) => (
                buttonTemplateProcessus(template, navigate, icon)
            ))}
        </div>
        </AnimatedList>
    )
}
export const ProcessusEditorPage = () => {

    const navigate = useNavigate();
    const [templatesProcessusEntree, setTemplatesProcessusEntree] = useState<TemplateProcessusDTO[]>([]);
    const [templatesProcessusMobilite, setTemplatesProcessusMobilite] = useState<TemplateProcessusDTO[]>([]);
    const [templatesProcessusSortie, setTemplatesProcessusSortie] = useState<TemplateProcessusDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Récupérer les templates de processus d'entrée et de mobilité interne
    useEffect(() => {
        async function fetchTemplates() {
            try {
                setLoading(true);
                setError(null);
                
                // Récupérer les templates d'entrée et de mobilité interne
                const [templatesEntree, templatesMobilite, templatesSortie] = await Promise.all([
                    templateProcessusService.getTemplatesByType('entree'),
                    templateProcessusService.getTemplatesByType('mobitliteInterne'),
                    templateProcessusService.getTemplatesByType('sortie'),
                ]);
                
                // Combiner les trois listes
                setTemplatesProcessusEntree(templatesEntree);
                setTemplatesProcessusMobilite(templatesMobilite);
                setTemplatesProcessusSortie(templatesSortie);
            } catch (err) {
                console.error('Erreur lors du chargement des templates:', err);
                setError('Erreur lors du chargement des templates. Veuillez réessayer.');
            } finally {
                setLoading(false);
            }
        }
        
        fetchTemplates();
    }, []);


    return ( <div className="p-8 flex flex-col gap-4 h-full min-h-0">
        <div>   
            <div className="mb-8 px-4 py-2 bg-secondary rounded-md shrink-0">
        <span className="font-semibold text-white">Liste des processus</span>
    </div>
            <h2>Processus d'entrée</h2>
            {listeTemplateProcessus(templatesProcessusEntree, navigate, getIconEntree())}
            <h2>Processus de mobilité interne</h2>
            {listeTemplateProcessus(templatesProcessusMobilite, navigate, getIconMobiliteInterne())}
            <h2>Processus de sortie</h2>
            {listeTemplateProcessus(templatesProcessusSortie, navigate, getIconSortie())}
        </div>
    </div>
    )
}