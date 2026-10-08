import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupInput,
} from "@/components/ui/input-group"
import { Search, SearchIcon } from "lucide-react";
import { Button } from "../ui/button";

type BarreRechercheProps = {
  value:string
  onChange: (value: string) => void
}
export  const BarreRecherche = ({value, onChange}: BarreRechercheProps) => 
{
    return (
      <InputGroup className="shadow-md rounded-lg min-w-fit w-full max-w-xl">
        <InputGroupInput placeholder="Recherche..." 
        value={value}
        onChange={(e) => onChange(e.target.value)}/>
        <InputGroupAddon>
        <Search/>
        </InputGroupAddon>
        <InputGroupAddon align="inline-end">
          <InputGroupButton variant="secondary" className="rounded-lg"><SearchIcon/></InputGroupButton>
        </InputGroupAddon>
      </InputGroup>

    );
}

export const ButtonFiltre = () => {
    return (
        <Button variant={"outline"}  size={"sm"} className="rounded-lg shadow-md w-fit max-w-[150px]">
            Filtre
        </Button>


    )
}