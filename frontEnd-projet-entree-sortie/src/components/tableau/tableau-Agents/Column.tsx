
"use client"
import  type { ColumnDef } from "@tanstack/react-table"
import { Button } from "@/components/ui/button"
import { Calendar,  UserRoundPen } from "lucide-react"

export type Agent = {
    nomPrenom:String
    direction:String
}

export type ListeAgent = {
    id: string,
    agent: Agent
    dateSortie:string
    etatAgent?: 'entree' | 'actif' | 'sortie' | 'mobiliteInterne' | 'ancien';
    role?: string;
}
export const columns: ColumnDef<ListeAgent>[] = [
  {
    accessorKey: "agent",
    header: ({column}) => {
      return (<Button variant="ghost" onClick={()=> column.toggleSorting(column.getIsSorted() === "asc") } className="hover:bg-transparent sticky left-0 z-0" >

        <UserRoundPen/>
        Utilisateur
      </Button>)
    },
    cell: ({row}) => {
       const agent = row.getValue("agent") as { nomPrenom: string, direction : string}
        return (<div className="grid grid-cols-1">
          <div className="font-bold truncate" title={agent.nomPrenom}>
            { agent.nomPrenom}
            </div>
          <div>
            {agent.direction}
          </div>
          </div>)
    },
    meta: {
      align: 'left'
    }
  },
  {
    accessorKey: "dateSortie",
    header: () => {
      return (<Button variant={"ghost"} className="sticky right-0 z-10 hover:bg-transparent w-full justify-center">
        <Calendar/>
        Date de Sortie
      </Button>)
    },
    cell: ({row}) => {
      const dateSortie = row.getValue("dateSortie") as string
      const formatDate = (dateString: string | null | undefined) => {
        if (!dateString || dateString.trim() === '') {
          return 'Non définie';
        }
        return dateString;
      };
      return <div>{formatDate(dateSortie)}</div>
    },
    meta: {
      align: 'center'
    }
  },
]