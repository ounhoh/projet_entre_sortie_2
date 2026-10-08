import { useEffect, useState, useRef } from "react"
import { useNavigate } from "react-router-dom"
import type {ListeAgent} from "./Column"
import {columns} from "./Column"
import { DataTable } from "../dateTable"
import { BarreRecherche } from "@/components/recherche/Recherche"
import { agentService } from "@/service/agentService"
import { mapAgentToListeAgent, filterActiveAgents } from "@/utils/agentMapper"
import { Button } from "@/components/ui/button"
import { ArrowUpDown, Filter } from "lucide-react"

const TableauListeAgent =  () => {
    const navigate = useNavigate();
    const [data,setData] = useState<ListeAgent[]>([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [sortOrder, setSortOrder] = useState<"asc" | "desc">("asc");
    const [filterType, setFilterType] = useState<"all" | "Agent" | "Prestataire" | "Conseiller">("all");
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const filterRef = useRef<HTMLDivElement>(null);

    const handleRowClick = (agent: ListeAgent) => {
        navigate(`/agent/${agent.id}`);
    };

    // Fonction pour déterminer le type d'agent
    const getAgentType = (role?: string): "Agent" | "Prestataire" | "Conseiller" => {
        if (!role) return "Agent";
        const roleUpper = role.toUpperCase();
        if (roleUpper === "PRESTATAIRE") return "Prestataire";
        if (roleUpper === "CONSEILLER") return "Conseiller";
        return "Agent";
    };

    // Fonction pour trier les données
    const sortedData = [...data].sort((a, b) => {
        const nameA = a.agent.nomPrenom.toLowerCase();
        const nameB = b.agent.nomPrenom.toLowerCase();
        return sortOrder === "asc" ? nameA.localeCompare(nameB) : nameB.localeCompare(nameA);
    });

    // Fonction pour filtrer les données
    const filteredData = sortedData.filter(agent => {
        if (filterType === "all") return true;
        return getAgentType(agent.role) === filterType;
    });

    useEffect(() => {
        async function fetchData(){
            try {
                setLoading(true);
                setError(null);
                const agentsData = await agentService.getAgents(0, 100);
                const mappedData = agentsData.map(mapAgentToListeAgent);
                // Filtrer pour ne garder que les agents actifs
                const activeAgents = filterActiveAgents(mappedData);
                setData(activeAgents);
            } catch (err) {
                console.error('Erreur lors du chargement des données:', err);
                setError('Erreur lors du chargement des données. Veuillez réessayer.');
                setData([]);
            } finally {
                setLoading(false);
            }
        }
        fetchData()
    },[])

    // Fermer le menu quand on clique ailleurs
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (filterRef.current && !filterRef.current.contains(event.target as Node)) {
                setIsFilterOpen(false);
            }
        };

        if (isFilterOpen) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => document.removeEventListener('mousedown', handleClickOutside);
        }
    }, [isFilterOpen]);
    return (
        <div className="p-8 gap-4 flex flex-col h-full min-h-0">
        <div className="mb-8 px-4 py-2 bg-secondary rounded-md shrink-0">
            <span className="font-semibold text-white">Liste des utilisateurs</span>
        </div> 

        <div className="mb-3 shrink-0 flex items-center gap-3">
            <div className="flex-1">
                <BarreRecherche value={search} onChange={setSearch}/> 
            </div>
            <Button
                variant="outline"
                size="sm"
                className="rounded-lg shadow-md w-fit whitespace-nowrap"
                onClick={() => setSortOrder(sortOrder === "asc" ? "desc" : "asc")}
                title={sortOrder === "asc" ? "Tri: A-Z" : "Tri: Z-A"}
            >
                <ArrowUpDown className="w-4 h-4 mr-2" />
                {sortOrder === "asc" ? "A-Z" : "Z-A"}
            </Button>
            <div className="relative" ref={filterRef}>
                <Button
                    variant="outline"
                    size="sm"
                    className="rounded-lg shadow-md w-fit whitespace-nowrap"
                    onClick={() => setIsFilterOpen(!isFilterOpen)}
                >
                    <Filter className="w-4 h-4 mr-2" />
                    Filtre
                </Button>
                {isFilterOpen && (
                    <div className="absolute right-0 mt-1 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                        <button
                            className={`block w-full text-left px-4 py-2 hover:bg-gray-100 first:rounded-t-lg ${filterType === "all" ? "bg-gray-50 font-semibold" : ""}`}
                            onClick={() => {
                                setFilterType("all");
                                setIsFilterOpen(false);
                            }}
                        >
                            Tous
                        </button>
                        <button
                            className={`block w-full text-left px-4 py-2 hover:bg-gray-100 ${filterType === "Agent" ? "bg-gray-50 font-semibold" : ""}`}
                            onClick={() => {
                                setFilterType("Agent");
                                setIsFilterOpen(false);
                            }}
                        >
                            Agent
                        </button>
                        <button
                            className={`block w-full text-left px-4 py-2 hover:bg-gray-100 ${filterType === "Prestataire" ? "bg-gray-50 font-semibold" : ""}`}
                            onClick={() => {
                                setFilterType("Prestataire");
                                setIsFilterOpen(false);
                            }}
                        >
                            Prestataire
                        </button>
                        <button
                            className={`block w-full text-left px-4 py-2 hover:bg-gray-100 last:rounded-b-lg ${filterType === "Conseiller" ? "bg-gray-50 font-semibold" : ""}`}
                            onClick={() => {
                                setFilterType("Conseiller");
                                setIsFilterOpen(false);
                            }}
                        >
                            Conseiller/Attaché
                        </button>
                    </div>
                )}
            </div>
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
            <div className="flex-1 min-h-0 flex flex-col">
                <DataTable columns={columns} data={filteredData} searchs={search} onRowClick={handleRowClick}/>
            </div>
        )}
        </div>
    )
}

export default TableauListeAgent