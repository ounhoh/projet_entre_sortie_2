import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Retour } from "../../../utils/Retour";
import { templateProcessusService, type TemplateProcessusDTO } from "@/service/templateProcessusService";
import { AnimatedList } from "../../../ui/animated-list";
import { cn } from "@/lib/utils";
import { FileText, CheckCircle2, Target } from "lucide-react";
import { getElement } from "./Element";


type ProcessType = "entree" | "sortie";

export function ContenuProcessPage({ type = "entree" }: { type?: ProcessType }) {
    const navigate = useNavigate();
    const location = useLocation();
    const [templatesProcessus, setTemplatesProcessus] = useState<TemplateProcessusDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Récupérer les templates de processus d'entrée et de mobilité interne
    useEffect(() => {
        async function fetchTemplates() {
            try {
                setLoading(true);
                setError(null);
                
                const allTemplates = type === "sortie"
                    ? await templateProcessusService.getTemplatesByType('sortie')
                    : await Promise.all([
                        templateProcessusService.getTemplatesByType('entree'),
                        templateProcessusService.getTemplatesByType('mobitliteInterne')
                    ]).then(([templatesEntree, templatesMobilite]) => [...templatesEntree, ...templatesMobilite]);
                setTemplatesProcessus(allTemplates);
            } catch (err) {
                console.error('Erreur lors du chargement des templates:', err);
                setError('Erreur lors du chargement des templates. Veuillez réessayer.');
            } finally {
                setLoading(false);
            }
        }
        
        fetchTemplates();
    }, [type]);

    const searchParams = new URLSearchParams(location.search);
    const modePrestataire = searchParams.get('mode') === 'prestataire';
    const modeConseiller = searchParams.get('mode') === 'conseiller';



    return (
        <div className="p-8 gap-4 flex flex-col h-full min-h-0 overflow-auto">
            {loading && (
                <div className="flex items-center justify-center p-8">
                    <p>Chargement des templates...</p>
                </div>
            )}

            {error && (
                <div className="flex items-center justify-center p-8 text-red-500">
                    <p>{error}</p>
                </div>
            )}

            {!loading && !error && (
                <>
                    <div className="mb-4">
                        <Retour/>
                        <h2 className="text-2xl font-bold">Création de processus</h2>
                        <p className="text-muted-foreground">
                            {type === "sortie" ? "Processus de sortie" : "Processus d'entrée et mobilité interne"}
                        </p>
                    </div>
                    
                    {templatesProcessus.length === 0 ? (
                        <div className="flex items-center justify-center p-8">
                            <p>Aucun template trouvé</p>
                        </div>
                    ) : (
                        <AnimatedList>
                        <div className="flex flex-wrap gap-4">
                            {templatesProcessus.map((template) => (
                                getElement(template, navigate, modePrestataire, modeConseiller)
                            ))}
                        </div>
                        </AnimatedList> 
                    )}
                </>
            )}
        </div>
    );
}
