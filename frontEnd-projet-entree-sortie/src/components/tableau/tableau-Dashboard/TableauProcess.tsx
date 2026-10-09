import type { Process } from "./Column"
import { getColumns } from "./Column"
import { BarreRecherche } from "@/components/recherche/Recherche"
import { AjouterAgentButton, AjouterListAgentButton, AjouterPrestataireButton, AjouterConseillerButton } from "@/components/NavBar/AjouterAgent"
// import { Card, CardContent } from "@/components/ui/card"
import { DataTableProcess } from "../dataTableProcess"
import { UserPlus, UserMinus, ArrowLeftRight, Users } from "lucide-react"
import { useMemo, useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"

interface TableauProcessProps {
    data: Process[];
    search: string;
    onSearchChange: (value: string) => void;
    fixedType?: Process["type"];
}

// Fonction de filtrage avec recherche et filtre par type
function filterProcesses(data: Process[], search: string, selectedType: string | null): Process[] {
    let filtered = data;

    // Filtre par type
    if (selectedType && selectedType !== 'all') {
        filtered = filtered.filter(process => process.type === selectedType);
    }

    // Filtre par recherche
    if (!search) return filtered;

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

    return filtered.filter(row => {
        return Object.values(row).some(value => searchInObject(value));
    });
}

const TableauProcess = ({ data, search, onSearchChange, fixedType }: TableauProcessProps) => {
    const navigate = useNavigate();
    const [selectedType, setSelectedType] = useState<string | null>(fixedType ?? "all");

    useEffect(() => {
        setSelectedType(fixedType ?? "all");
    }, [fixedType]);

    const dateHeaderLabel = useMemo(() => {
        if (selectedType === 'sortie') {
            return "Date de Sortie";
        }
        if (selectedType === 'entree' || selectedType === 'mobilite_interne') {
            return "Date d'Arrivée";
        }
        return "Date de Mobilisation";
    }, [selectedType]);

    const columns = useMemo(() => getColumns(dateHeaderLabel), [dateHeaderLabel]);

    // Filtrer les données selon la recherche et le type
    const filteredData = useMemo(() => filterProcesses(data, search, selectedType), [data, search, selectedType]);

    // Compter les processus par type
    const counts = useMemo(() => {
        return {
            all: data.length,
            entree: data.filter(p => p.type === 'entree').length,
            sortie: data.filter(p => p.type === 'sortie').length,
            mobilite_interne: data.filter(p => p.type === 'mobilite_interne').length,
        };
    }, [data]);

    // Handler pour le clic sur une ligne
    const handleRowClick = (row: Process) => {
        navigate(`/processus/${row.id}`);
    };

    return (
        <div className="flex flex-col h-full min-h-0 w-full">
            <div className="mb-8 px-4 py-2 bg-secondary rounded-md shrink-0">
                <span className="font-semibold text-white">Liste des processus</span>
            </div>

            {/* Cartes de catégories - Commentées */}
            {/* <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4 shrink-0">
                <Card 
                    className={`cursor-pointer transition-all hover:shadow-lg ${selectedType === 'all' ? 'ring-2 ring-primary' : ''}`}
                    onClick={() => setSelectedType('all')}
                >
                    <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-2">
                                    <div className="w-10 h-10 rounded-lg bg-blue-500 flex items-center justify-center shrink-0">
                                        <Users className="w-5 h-5 text-white" />
                                    </div>
                                    <span className="font-semibold text-lg">Tous</span>
                                </div>
                                <div className="text-3xl font-bold text-blue-600">
                                    {counts.all}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card 
                    className={`cursor-pointer transition-all hover:shadow-lg ${selectedType === 'entree' ? 'ring-2 ring-primary' : ''}`}
                    onClick={() => setSelectedType('entree')}
                >
                    <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-2">
                                    <div className="w-10 h-10 rounded-lg bg-green-500 flex items-center justify-center shrink-0">
                                        <UserPlus className="w-5 h-5 text-white" />
                                    </div>
                                    <span className="font-semibold text-lg">Entrée</span>
                                </div>
                                <div className="text-3xl font-bold text-green-600">
                                    {counts.entree}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card 
                    className={`cursor-pointer transition-all hover:shadow-lg ${selectedType === 'sortie' ? 'ring-2 ring-primary' : ''}`}
                    onClick={() => setSelectedType('sortie')}
                >
                    <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-2">
                                    <div className="w-10 h-10 rounded-lg bg-red-500 flex items-center justify-center shrink-0">
                                        <UserMinus className="w-5 h-5 text-white" />
                                    </div>
                                    <span className="font-semibold text-lg">Sortie</span>
                                </div>
                                <div className="text-3xl font-bold text-red-600">
                                    {counts.sortie}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card 
                    className={`cursor-pointer transition-all hover:shadow-lg ${selectedType === 'mobilite_interne' ? 'ring-2 ring-primary' : ''}`}
                    onClick={() => setSelectedType('mobilite_interne')}
                >
                    <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-2">
                                    <div className="w-10 h-10 rounded-lg bg-amber-500 flex items-center justify-center shrink-0">
                                        <ArrowLeftRight className="w-5 h-5 text-white" />
                                    </div>
                                    <span className="font-semibold text-lg">Mobilité Interne</span>
                                </div>
                                <div className="text-3xl font-bold text-amber-600">
                                    {counts.mobilite_interne}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div> */}

            {/* Filtres de catégories - Version boutons */}
            {!fixedType && (

                <div className="flex flex-wrap items-center gap-2 mb-4 shrink-0">
                    <Button
                        variant={selectedType === 'all' ? 'default' : 'outline'}
                        onClick={() => setSelectedType('all')}
                        className="flex items-center gap-2 h-10 px-4"
                    >
                        <Users className="w-4 h-4 shrink-0" />
                        Tous ({counts.all})
                    </Button>
                    <Button
                        variant={selectedType === 'entree' ? 'default' : 'outline'}
                        onClick={() => setSelectedType('entree')}
                        className="flex items-center gap-2 h-10 px-4"
                    >
                        <div className="w-4 h-4 shrink-0 rounded bg-green-500 flex items-center justify-center">
                            <UserPlus className="w-3 h-3 text-white" />
                        </div>
                        Entrée ({counts.entree})
                    </Button>
                    <Button
                        variant={selectedType === 'sortie' ? 'default' : 'outline'}
                        onClick={() => setSelectedType('sortie')}
                        className="flex items-center gap-2 h-10 px-4"
                    >
                        <div className="w-4 h-4 shrink-0 rounded bg-red-500 flex items-center justify-center">
                            <UserMinus className="w-3 h-3 text-white" />
                        </div>
                        Sortie ({counts.sortie})
                    </Button>
                    <Button
                        variant={selectedType === 'mobilite_interne' ? 'default' : 'outline'}
                        onClick={() => setSelectedType('mobilite_interne')}
                        className="flex items-center gap-2 h-10 px-4"
                    >
                        <div className="w-4 h-4 shrink-0 rounded bg-amber-500 flex items-center justify-center">
                            <ArrowLeftRight className="w-3 h-3 text-white" />
                        </div>
                        Mobilité Interne ({counts.mobilite_interne})
                    </Button>
                </div>

            )}

            <div className="grid grid-cols-1 sm:grid-cols-1 lg:grid-cols-2 mb-3 shrink-0">
                <BarreRecherche value={search} onChange={onSearchChange} />
                <div className="flex justify-end gap-2">
                    <AjouterAgentButton />
                    <AjouterPrestataireButton />
                    <AjouterConseillerButton />
                </div>
            </div>

            {/* DataTable */}
            <div className="flex-1 min-h-0 flex flex-col">
                <DataTableProcess
                    columns={columns}
                    data={filteredData}
                    searchs={search}
                    onRowClick={handleRowClick}
                />
            </div>
        </div>
    )
}

export default TableauProcess
