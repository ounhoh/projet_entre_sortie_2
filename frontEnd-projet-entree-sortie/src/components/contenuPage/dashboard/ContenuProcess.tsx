import { useState, useEffect, useMemo, type JSX } from "react";
import TableauProcess from "../../tableau/tableau-Dashboard/TableauProcess";
import { Card, CardContent, CardHeader, CardTitle } from "../../ui/card";
import { getIconAllProcess, getIconEntree, getIconMobiliteInterne, getIconSortie } from "../../iconTypeProcessus/iconTypeProcessus";
import type { Process } from "../../tableau/tableau-Dashboard/Column";
import { processusService } from "@/service/processusService";
import { mapProcessusToProcess } from "@/utils/processusMapper";
import { useNavigate } from "react-router-dom";

function StatCard({description, valeur, icon} : {description: string ; valeur: number; icon:JSX.Element})
{
    return (
        <Card className="w-full max-w-xs gap-0 overflow-hidden border-slate-200 bg-white py-0 shadow-sm dark:border-border dark:bg-card">
            <CardHeader className="px-5 pb-0 pt-5">
                <CardTitle className="text-sm font-medium text-slate-600 dark:text-muted-foreground">{description}</CardTitle>
            </CardHeader>
            <CardContent className="relative flex items-center justify-between px-5 pb-5 pt-3">
                <div className="relative z-10 flex w-full items-center justify-between">
                    {/* Grand nombre à gauche */}
                    <div className="flex flex-col">
                        <div className="text-4xl font-semibold tracking-tight text-slate-900 dark:text-foreground">{valeur}</div>
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

    //console.log("filteredData :", filteredData);

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
        <div className="flex h-full min-h-0 flex-col gap-5 bg-slate-50 p-6 lg:p-8 dark:bg-background">
            <div className="grid shrink-0 grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <StatCard description="Processus en cours" valeur={entreNombre + sortieNombre + mobiliteNombre} icon={getIconAllProcess(() => navigate("/processus-editor"))}/>
                <StatCard description="Entrée" valeur={entreNombre} icon={getIconEntree(() => navigate("/process_entree"))}/>
                <StatCard description="Sortie" valeur={sortieNombre} icon={getIconSortie(() => navigate("/process_sortie"))}/>
                <StatCard description="Mobilité Interne" valeur={mobiliteNombre} icon={getIconMobiliteInterne(() => navigate("/mobilite-interne"))}/>
            </div>
           

            {loading && (
                <div className="flex items-center justify-center p-8">
                    <p>Chargement des données...</p>
                </div>
            )}
            
            {error && (
                <div className="flex items-center justify-center p-8 text-sm text-red-700 dark:text-red-300">
                    <p>{error}</p>
                </div>
            )}

            {!loading && !error && (
                <div className="flex w-full min-h-0 flex-1 flex-col">
                    <TableauProcess data={data} search={search} onSearchChange={setSearch}/>
                </div>
            )}
        </div>
    )
}