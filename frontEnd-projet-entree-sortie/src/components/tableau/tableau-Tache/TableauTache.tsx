import { useEffect, useState } from "react"
import type {InfoGroupeTache, } from "./Column"
import {columns} from "./Column"
import { DataTable } from "../dateTable"
import { BarreRecherche, ButtonFiltre } from "@/components/recherche/Recherche"
import { AjouterAgentButton, AjouterListAgentButton } from "@/components/NavBar/AjouterAgent"
import { DataTableProcess } from "../dataTableProcess"

const getData = async (): Promise<InfoGroupeTache[]> => [
    {

    id: "728ed534",
    description: "teste tache",
    statut: "fait",
    dateEcheance:"01/02/25", 
    
    },
]


const TableauTache =  () => {
    const [data,setData] = useState<InfoGroupeTache[]>([]);
    const [search, setSearch] = useState("");
    useEffect(() => {
        async function fetchData(){
            const res = await getData();
            setData(res)
        }
        fetchData()
    },[])
    return (
        <div className="grid grid-cols-1 gap-4">

        
        <BarreRecherche value={search} onChange={setSearch}/> 
        <div>
        <DataTableProcess columns={columns} data={data} searchs={search}/>
</div>
        </div>
    )
}

export default TableauTache