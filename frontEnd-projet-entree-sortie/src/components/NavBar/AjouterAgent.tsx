import { BookText, Download, Plus, User } from "lucide-react";


import * as React from "react"
import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { getCurrentAgent, getCurrentAgentDisplay } from "@/utils/currentAgent"
import { FieldGroup, FieldLegend, FieldSet,Field,FieldLabel } from "../ui/field";
import { DialogClose } from "@radix-ui/react-dialog";



function ProfileForm({className} : React.ComponentProps<"form">){
  return (
  <form className={className}>
    <FieldGroup>
    <FieldSet>
      <FieldLegend className="items-center" >
        Ajout d'une liste d'agent
      </FieldLegend>
      <FieldGroup>
        <Field>
        <FieldLabel className="flex">
          <Download className="w-4 h-4"/>
         Télécharger le modèle d'import
        </FieldLabel>
        <Button  variant={"outline"} type="button">
          <Download className="w-4 h-4 mr-2"/>
          télécharger
        </Button>
        </Field>
        <Field>
          <FieldLabel>
            <BookText className="w-4 h-4"/>
            Importer des agents
          </FieldLabel>
          <Input id="telechargement_fichier" type="file" />
        </Field>
      </FieldGroup>
    </FieldSet>
    </FieldGroup>
    </form>
  );
}
export const AjouterListAgentButton = () => {
    const [open,setOpen] = React.useState(false);
    const user = getCurrentAgentDisplay();
   return (        
    <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
          <Button  size={"sm"}   className=" rounded-lg w-fit">
            <Plus className="w-4 h-4 mr-2"/>
            <span className="text-center">Ajouter une liste d'agent</span>
          </Button>
        </DialogTrigger>
        <DialogContent>
            <DialogHeader>
              <DialogTitle className="flex">
                <User className="w-7 h-7"/>
                { user.nom }
              </DialogTitle>
              <DialogDescription>
                  { user.direction }
              </DialogDescription>
            </DialogHeader>
            <ProfileForm/>
      <DialogFooter>
        <DialogClose asChild>
          <Button variant={"outline"}>Annuler</Button>
        </DialogClose>
          <Button  type="submit" onClick={() => {
            setOpen(false);
          } }>Appliquer</Button>
      </DialogFooter>
        </DialogContent>
    </Dialog>
   );
}



export const AjouterAgentButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddAgent = direction === "drh";

  if (!canAddAgent) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree">
        <Plus />
        Ajouter des Agents
      </Link>
    </Button>
  );
}

export const AjouterPrestataireButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddPrestataire = direction === "dappi" || direction === "dsiun" || direction === "daf";

  if (!canAddPrestataire) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree?mode=prestataire">
        <Plus />
        Ajouter des Prestataires
      </Link>
    </Button>
  );
}

export const AjouterConseillerButton = () => {
  const agent = getCurrentAgent();
  const direction = (agent.direction || "").trim().toLowerCase();
  const canAddConseiller = direction === "daf" || direction === "dsiun" || direction === "sg";

  if (!canAddConseiller) {
    return null;
  }

  return (
    <Button asChild size="sm" className="rounded-lg w-fit">
      <Link to="/process_entree?mode=conseiller">
        <Plus />
        Ajouter des Conseillers/Attachés
      </Link>
    </Button>
  );
}
