
"use client"
import  type { ColumnDef } from "@tanstack/react-table"
import { Button } from "@/components/ui/button"
import { ArrowDown,  Calendar1, CalendarClock, NotebookPen, Target, UserRoundPen, Users } from "lucide-react"
import { getIconEntree, getIconMobiliteInterne, getIconSortie } from "@/components/iconTypeProcessus/iconTypeProcessus"
import { Badge } from "@/components/ui/badge"
import { getBadgeAfaire, getBadgeEnCours, getBadgeFait } from "@/components/utils/Statut"
export type Agent = {
    nomPrenom:String
    direction:String
}

export type InfoGroupeTache = {
    id: string,
    description:string
    statut: "en cours" | "a faire" | "fait",
    dateEcheance:string
}

export const columns: ColumnDef<InfoGroupeTache>[] = [ 
  {
    accessorKey: "description",
    header: ({column}) => {
      return (<Button variant="ghost">
        <NotebookPen/>
        Tache
      </Button>)
    },
  },
  {
    accessorKey: "statut",
    header: ({column}) => {
      return (<Button variant="ghost">
        <ArrowDown/>
        Statut
      </Button>)
    },
    cell: ({row}) => {
      const value = row.getValue("statut") as InfoGroupeTache["statut"]
      return (
        <>
        {value==="a faire" && getBadgeAfaire()}
        {value==="en cours" && getBadgeEnCours()}
        {value==="fait" && getBadgeFait()}

        </>
      )
    }
  },
  {
    accessorKey: "dateEcheance",
    header: ({column}) => {
      return (<Button variant={"ghost"}>
        <CalendarClock/>
        Date d'Echéance
      </Button>)
    },
  },
]