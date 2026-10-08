import { useState, useEffect, useMemo, type JSX } from "react";
import TableauProcess from "../../tableau/tableau-Dashboard/TableauProcess";
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card";
import { getIconAllProcess, getIconEntree, getIconMobiliteInterne, getIconSortie } from "../../iconTypeProcessus/iconTypeProcessus";
import type { Process } from "../../tableau/tableau-Dashboard/Column";
import { processusService } from "@/service/processusService";
import { mapProcessusToProcess } from "@/utils/processusMapper";
import { useNavigate } from "react-router-dom";

function StatCard({description, valeur, icon, headerColor} : {description: string ; valeur: number; icon:JSX.Element; headerColor?: string})
{
    return (
        <Card className="w-full max-w-xs shadow-sm pt-0 gap-0 overflow-hidden relative">
            <CardHeader className={`pt-4 px-6 pb-3 rounded-t-xl ${headerColor ? headerColor : "bg-blue-500"} mx-0`}>
                <CardTitle className="text-sm font-medium text-white">{description}</CardTitle>
            </CardHeader>
            <CardContent className="relative px-6 py-6 ">
                {/* Motif d'onde décoratif en bas à droite */}
                <div className="absolute bottom-0 right-0 w-48 h-20 opacity-10">
                    <svg viewBox="0 0 300 120" xmlns="http://www.w3.org/2000/svg" className="w-full h-full" preserveAspectRatio="none">
                        <path d="M0,60 Q75,30 150,60 T300,60 T450,60" stroke="currentColor" strokeWidth="2" fill="none" className="text-cese-bleu-principal dark:text-blue-200"/>
                        <path d="M0,75 Q75,45 150,75 T300,75 T450,75" stroke="currentColor" strokeWidth="2" fill="none" className="text-cese-bleu-secondaire dark:text-blue-300"/>
                        <path d="M0,90 Q75,60 150,90 T300,90 T450,90" stroke="currentColor" strokeWidth="2" fill="none" className="text-cese-rouge-secondaire dark:text-red-200"/>
                        <path d="M0,105 Q75,75 150,105 T300,105 T450,105" stroke="currentColor" strokeWidth="2" fill="none" className="text-cese-bleu-principal dark:text-blue-200"/>
                    </svg>
                </div>
                
                <div className="flex items-start justify-between relative z-10">
                    {/* Grand nombre à gauche */}
                    <div className="flex flex-col">
                        <div className="text-5xl font-bold">{valeur}</div>
                    </div>
                    
                    {/* Icône à droite */}
                    <div className="shrink-0">
                        {icon}
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}


export function BodyPage() {
    const navigate = useNavigate();
    const [data, setData] = useState<Process[]>([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Charger les données depuis l'API
    useEffect(() => {
        async function fetchData(){
            try {
                setLoading(true);
                setError(null);
                const processusData = await processusService.getProcessusActifs();
                const mappedData = processusData.map(mapProcessusToProcess);
                setData(mappedData);
            } catch (err) {
                console.error('Erreur lors du chargement des données:', err);
                setError('Erreur lors du chargement des données. Veuillez réessayer.');
                setData([]);
            } finally {
                setLoading(false);
            }
        }
        fetchData()
    }, [])

    // Fonction de filtrage (identique à celle du tableau)
    const filteredData = useMemo(() => {
        if (!search) return data;
        
        const searchValue = search.toLowerCase().trim();
        
        const searchInObject = (obj: any): boolean => {
            if (obj === null || obj === undefined) return false;
            
            if (typeof obj === 'string') {
                return obj.toLowerCase().includes(searchValue);
            }
            
            if (typeof obj === 'object') {
                return Object.values(obj).some(value => {
                    if (typeof value === 'string') {
                        return value.toLowerCase().includes(searchValue);
                    }
                    if (typeof value === 'object' && value !== null) {
                        return searchInObject(value);
                    }
                    return false;
                });
            }
            
            return String(obj).toLowerCase().includes(searchValue);
        };

        
        return data.filter(row => {
            return Object.values(row).some(value => searchInObject(value));
        });
    }, [data, search]);

    console.log("filteredData :", filteredData);

    // Calculer les statistiques basées sur les données filtrées
    const entreNombre = useMemo(() => 
        filteredData.filter(item => item.type === "entree").length,
        [filteredData]
    );
    
    const sortieNombre = useMemo(() => 
        filteredData.filter(item => item.type === "sortie").length,
        [filteredData]
    );
    
    const mobiliteNombre = useMemo(() => 
        filteredData.filter(item => item.type === "mobilite_interne").length,
        [filteredData]
    );

    return (
        <div className="p-8 flex flex-col gap-4 h-full min-h-0">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 shrink-0">
                <StatCard description="Processus en cours" valeur={entreNombre + sortieNombre + mobiliteNombre} icon={getIconAllProcess()} headerColor="bg-blue-500"/>
                <StatCard description="Entrée" valeur={entreNombre} icon={getIconEntree(() => navigate("/process_entree"))} headerColor="bg-green-500"/>
                <StatCard description="Sortie" valeur={sortieNombre} icon={getIconSortie(() => navigate("/process_sortie"))} headerColor="bg-red-500"/>
                <StatCard description="Mobilité Interne" valeur={mobiliteNombre} icon={getIconMobiliteInterne()} headerColor="bg-amber-500"/>
            </div>
           

            {loading && (
                <div className="flex items-center justify-center p-8">
                    <p>Chargement des données...</p>
                </div>
            )}
            
            {error && (
                <div className="flex items-center justify-center p-8 text-red-500">
                    <p>{error}</p>
                </div>
            )}

            {!loading && !error && (
                <div className="w-full rounded-lg flex-1 min-h-0 flex flex-col">
                    <TableauProcess data={data} search={search} onSearchChange={setSearch}/>
                </div>
            )}
        </div>
    )
}