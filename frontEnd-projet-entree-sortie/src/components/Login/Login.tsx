import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Field,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import logo from "@/assets/logo-CESE-blanc-EXE_baseline.png";
import { agentService } from "@/service/agentService";
import type { AgentDTO } from "@/service/processusService";
import { getDirectionDisplay } from "@/utils/directionDisplay";

type LoadState = "idle" | "loading" | "error";

const STORAGE_KEY = "currentAgent";

function DirectionAgentSelector({ className }: React.ComponentProps<"div">) {
  const navigate = useNavigate();
  const [state, setState] = useState<LoadState>("idle");
  const [error, setError] = useState<string | null>(null);
  const [agents, setAgents] = useState<AgentDTO[]>([]);

  useEffect(() => {
    const load = async () => {
      setState("loading");
      setError(null);
      try {
        const data = await agentService.getAllAgentsActifs();
        setAgents(data);
        setState("idle");
      } catch (e: any) {
        setError(e?.message || "Erreur lors du chargement");
        setState("error");
      }
    };
    load();
  }, []);

  const agentsTries = useMemo(() => {
    return [...agents]
      .filter((agent) => String(agent.etatAgent || "").trim().toLowerCase() === "actif")
      .sort((a, b) => {
      const nomA = `${a.nom || ""} ${a.prenom || ""}`.trim().toLowerCase();
      const nomB = `${b.nom || ""} ${b.prenom || ""}`.trim().toLowerCase();
      return nomA.localeCompare(nomB);
    });
  }, [agents]);

  const handleSelectAgent = (agent: AgentDTO) => {
    const payload = {
      id: agent.id,
      nom: agent.nom,
      prenom: agent.prenom,
      direction: agent.direction,
      code: agent.code,
      role: agent.role,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    navigate("/dashboard");
  };

  return (
    <div className={cn("flex flex-col justify-center gap-6 p-8", className)}>
      <Card>
        <CardHeader>
          <CardTitle className="text-center">Agents actifs</CardTitle>
          <CardDescription className="text-center">
            Choisissez un agent pour vous connecter
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {state === "loading" && (
            <p className="text-center text-sm text-muted-foreground">Chargement...</p>
          )}
          {state === "error" && (
            <p className="text-center text-sm text-red-500">{error}</p>
          )}
          {state === "idle" && agentsTries.length === 0 && (
            <p className="text-center text-sm text-muted-foreground">
              Aucun agent actif trouvé.
            </p>
          )}
          {agentsTries.map((agent) => (
            <Button
              key={agent.id}
              variant="outline"
              className="w-full justify-between"
              type="button"
              onClick={() => handleSelectAgent(agent)}
            >
              <span className="truncate">
                {agent.prenom} {agent.nom}
              </span>
              <span className="ml-3 text-xs text-muted-foreground">
                {getDirectionDisplay({
                  direction: agent.direction,
                  role: agent.role,
                }) || "Direction inconnue"}
              </span>
            </Button>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  return (
    <div className={cn("flex flex-col justify-center gap-6 p-8", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle className="text-center">Bienvenue</CardTitle>
          <CardDescription className="text-center">
            Connectez-vous pour continuer
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">Adresse mail</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  placeholder="m@example.com"
                />
              </Field>
              <Field>
                <div className="flex items-center">
                  <FieldLabel htmlFor="password">Mot de passe</FieldLabel>
                </div>
                <Input id="password" type="password" />
              </Field>
              <Field>
                <Link to={"/dashboard"} className="">
                  <Button type="submit" className="bg-cese-bleu-secondaire hover:bg-cese-bleu-principal w-full">
                    Se connecter
                  </Button>
                </Link>

                <Button variant="outline" className="text-cese-rouge-secondaire hover:text-red-600" type="button">
                  <span>Récupération de mot de passe</span>
                </Button>

                <Link to={"/dashboard"}>
                  <Button className="bg-cese-bleu-secondaire hover:bg-cese-bleu-principal w-full">
                    Se connecter avec le SSO
                  </Button>
                </Link>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export default function PageLogin() {
  return (
    <div className="min-h-screen w-full bg-cese-bleu-principal flex flex-col items-center justify-center p-6">
      <img src={logo} width={255} height={255} alt="logo" />
      <div className="flex min-h-svh w-full justify-center p-6 md:p-10">
        <div className="w-full max-w-sm space-y-6">
          <LoginForm />
          <DirectionAgentSelector />
        </div>
      </div>
    </div>
  );
}