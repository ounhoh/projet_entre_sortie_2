import { Calendar, ImageIcon, NotebookPen, Table } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "../ui/card";
import { Separator } from "@radix-ui/react-separator";
import { Input } from "../ui/input";
import { Label } from "../ui/label";


type Tache = {
    titre: string,
    statut: "a faire" | "fait" | "en cours",
    description:string
}
export function CardTache(element:Tache)
{
    return (<Card className="w-full max-w-xs shadow-sm">

                                    <CardHeader className="pb-2">
                                        <CardTitle className="text-sm font-medium text-muted-foreground">{element.description}</CardTitle>
                                   <Separator/> 
                                    </CardHeader>
                                <CardContent className="flex items-center justify-between">
                                {element.description}

                                </CardContent>
                                </Card>);
}


const AddTexte = ({texte,placeholder,required = false}:{texte:string,placeholder?:string,required?:boolean}) => {
    return (
        <div>
            <div className="flex items-center gap-2">
            <NotebookPen className="w-4 h-4" />
            <Label>{texte}</Label>
            </div>
            <Input type="text" placeholder={placeholder || "..."} required={required || false} />
        </div>
    )
}

const AddDate = ({date,placeholder,minDate,required = false}:{date:string,placeholder?:string,minDate?:Date,required?:boolean}) => {
    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            <Label>{date}</Label>

            </div>
            <Input type="date" placeholder={placeholder || "..."} min={minDate?.toISOString() || undefined} required={required || false} />
        </div>
    )
}

const AddImage = ({image,placeholder,required = false}:{image:string,placeholder?:string,required?:boolean}) => {
    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
            <ImageIcon className="w-4 h-4" />
            <Label>{image}</Label>
            </div>
            <Input type="file" placeholder={placeholder || "..."} required={required || false} />
        </div>
    )
}
export function CardFormulaire()
{
    const texte : string = "Nom de la tache";
    const date : string = "Date de la tache";
    const image : string = "Image de la tache";
    return (
        <Card className="w-full max-w-xs shadow-sm">
            <CardHeader>
                <CardTitle>Forumaire</CardTitle>
            </CardHeader>
            <CardContent>
                <AddTexte texte={texte} placeholder="..." />
                <AddDate date={date} minDate={new Date()} />
                <AddImage image={image} placeholder="..." />

            </CardContent>
        </Card>
    )
}

export function AffichageFonction(listeTache : Tache[]) {
    const lenght : number= listeTache.length;
   return (
    <div className="grid grid-cols-1">
        { listeTache.map((value,index) =>
            {
                return (<>

                </>);
            }
        )

        }
    </div>
   ) 
}
