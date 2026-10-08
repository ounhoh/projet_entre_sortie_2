import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";

interface RetourProps {
  to?: string; // Route optionnelle vers laquelle retourner
}

export function Retour({ to }: RetourProps) {
  const navigate = useNavigate();
  
  const handleRetour = () => {
    if (to) {
      navigate(to);
    } else {
      navigate(-1); // Par défaut, retourne à la page précédente
    }
  };
  
  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={handleRetour}
      className="gap-2"
    >
      <ArrowLeft className="h-4 w-4" />
      Retour
    </Button>
  );
}