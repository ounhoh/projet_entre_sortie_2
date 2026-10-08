import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { BarreRecherche } from "@/components/recherche/Recherche";
import { type Notifications } from "@/components/tableau/tableau-Notification/Column";
import { Badge } from "@/components/ui/badge";
import { AnimatedList, AnimatedListItem } from "@/components/ui/animated-list";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { ChevronDown, ChevronRight, UserRoundPen } from "lucide-react";
import { getBadgeAlerte, getBadgeInfo, getBadgeTache } from "@/components/utils/NiveauNotification";
import { notificationService } from "@/service/notificationService";
import { getCurrentAgent } from "@/utils/currentAgent";

type Agent = {
    nomPrenom: string
    direction: string
}

type NotificationParAgent = {
    agent: Agent;
    notifications: Notifications[];
}

const TableauNotification = () => {
    const navigate = useNavigate();
    const [data, setData] = useState<Notifications[]>([]);
    const [search, setSearch] = useState("");
    const [expandedAgents, setExpandedAgents] = useState<Set<string>>(new Set());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchData() {
            try {
                setLoading(true);
                setError(null);
                // Récupérer l'ID de l'agent depuis l'utilisateur connecté
                const agentId = getCurrentAgent().id;
                if (!agentId) {
                    throw new Error("ID agent non disponible");
                }
                const res = await notificationService.getNotificationsByAgent(agentId);
                setData(res);
            } catch (err: any) {
                console.error('Erreur lors du chargement des notifications:', err);
                setError(err?.message || 'Erreur lors du chargement des notifications');
                setData([]);
            } finally {
                setLoading(false);
            }
        }
        fetchData();
    }, []);

    // Grouper les notifications par agent
    const notificationsParAgent = useMemo(() => {
        const grouped = new Map<string, NotificationParAgent>();

        data.forEach(notification => {
            const agentKey = `${notification.agent.nomPrenom}_${notification.agent.direction}`;
            
            if (!grouped.has(agentKey)) {
                grouped.set(agentKey, {
                    agent: notification.agent,
                    notifications: []
                });
            }
            
            grouped.get(agentKey)!.notifications.push(notification);
        });

        return Array.from(grouped.values());
    }, [data]);

    // Filtrer par recherche
    const filteredNotificationsParAgent = useMemo(() => {
        if (!search.trim()) {
            return notificationsParAgent;
        }

        const searchLower = search.toLowerCase();
        return notificationsParAgent.filter(item =>
            item.agent.nomPrenom.toLowerCase().includes(searchLower) ||
            item.agent.direction.toLowerCase().includes(searchLower) ||
            item.notifications.some(n =>
                n.Description.toLowerCase().includes(searchLower) ||
                n.Direction.toLowerCase().includes(searchLower)
            )
        );
    }, [notificationsParAgent, search]);

    const toggleAgent = (agentKey: string) => {
        setExpandedAgents(prev => {
            const newSet = new Set(prev);
            if (newSet.has(agentKey)) {
                newSet.delete(agentKey);
            } else {
                newSet.add(agentKey);
            }
            return newSet;
        });
    };

    const getAgentKey = (agent: Agent) => `${agent.nomPrenom}_${agent.direction}`;

    if (loading) {
        return (
            <div className="flex items-center justify-center p-8">
                <p>Chargement des notifications...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center p-8 text-red-500">
                <p>{error}</p>
            </div>
        );
    }

    return (
        <div className="gap-4 p-4 flex flex-col h-full min-h-0">
            <div className="shrink-0 mb-2">
                <BarreRecherche value={search} onChange={setSearch} />
            </div>
            <div className="flex-1 overflow-y-auto min-h-0">
                <AnimatedList className="w-full">
                    {filteredNotificationsParAgent.map((item) => {
                        const agentKey = getAgentKey(item.agent);
                        const isExpanded = expandedAgents.has(agentKey);
                        const notificationCount = item.notifications.length;

                        return (
                            <AnimatedListItem key={agentKey}>
                                <Card className="w-full cursor-pointer hover:shadow-md transition-shadow">
                                    <CardHeader
                                        className="pb-3"
                                        onClick={() => toggleAgent(agentKey)}
                                    >
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                                {isExpanded ? (
                                                    <ChevronDown className="size-5 shrink-0" />
                                                ) : (
                                                    <ChevronRight className="size-5 shrink-0" />
                                                )}
                                                <UserRoundPen className="size-5 shrink-0" />
                                                <div className="flex flex-col min-w-0 flex-1">
                                                    <div className="font-semibold truncate" title={item.agent.nomPrenom}>
                                                        {item.agent.nomPrenom}
                                                    </div>
                                                    <div className="text-sm text-muted-foreground">
                                                        {item.agent.direction}
                                                    </div>
                                                </div>
                                            </div>
                                            <Badge variant="secondary" className="shrink-0">
                                                {notificationCount} notification{notificationCount > 1 ? 's' : ''}
                                            </Badge>
                                        </div>
                                    </CardHeader>
                                    {isExpanded && (
                                        <CardContent className="pt-0 space-y-2">
                                            {item.notifications.map((notification) => (
                                                <div
                                                    key={notification.id}
                                                    onClick={(e) => {
                                                        e.stopPropagation(); // Empêcher le toggle de l'agent
                                                        if (notification.processusId) {
                                                            navigate(`/processus/${notification.processusId}`);
                                                        }
                                                    }}
                                                    className={cn(
                                                        "p-3 rounded-lg border bg-muted/50 space-y-2 transition-colors",
                                                        notification.processusId && "cursor-pointer hover:bg-muted hover:border-primary/50"
                                                    )}
                                                >
                                                    <div className="flex items-center justify-between gap-2">
                                                        <div className="flex items-center gap-2">
                                                            {notification.Niveau === "tache" && getBadgeTache()}
                                                            {notification.Niveau === "information" && getBadgeInfo()}
                                                            {notification.Niveau === "alerte" && getBadgeAlerte()}
                                                            <span className="text-sm font-medium">
                                                                {notification.dateEnvoie}
                                                            </span>
                                                        </div>
                                                        <Badge variant="outline">
                                                            {notification.Direction}
                                                        </Badge>
                                                    </div>
                                                    <p className="text-sm text-muted-foreground">
                                                        {notification.Description}
                                                    </p>
                                                </div>
                                            ))}
                                        </CardContent>
                                    )}
                                </Card>
                            </AnimatedListItem>
                        );
                    })}
                </AnimatedList>
            </div>
        </div>
    );
}

export default TableauNotification