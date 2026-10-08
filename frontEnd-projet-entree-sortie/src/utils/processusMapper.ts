import type { ProcessusDTO } from '@/service/processusService';
import type { Process } from '@/components/tableau/tableau-Dashboard/Column';
import { getDirectionDisplay } from './directionDisplay';

// Fonction pour mapper ProcessusDTO vers Process
export function mapProcessusToProcess(processus: ProcessusDTO): Process {
  // Mapper le type (gérer la faute de frappe dans le backend)
  let type: "entree" | "sortie" | "mobilite_interne";
  if (processus.typeProcessus === "entree") {
    type = "entree";
  } else if (processus.typeProcessus === "sortie") {
    type = "sortie";
  } else {
    // "mobitliteInterne" -> "mobilite_interne"
    type = "mobilite_interne";
  }

  // Mapper le statut selon le code ou libelle
  let statut: string;
  const statutCode = processus.statut.code.toLowerCase();
  const statutLibelle = processus.statut.libelle.toLowerCase();
  /* 
  // Vérifier d'abord les statuts terminés/clôturés
  if (statutCode.includes("success") || statutCode.includes("termine") || 
      statutCode.includes("cloture") || statutCode.endsWith("end") ||
      statutLibelle.includes("terminé") || statutLibelle.includes("clôturé") ||
      statutLibelle.includes("processus cloturé")) {
    statut = "success";
  } 
  // Vérifier les statuts en attente
  else if (statutCode.includes("pending") || statutCode.includes("en_attente") || 
           statutLibelle.includes("en attente") || statutLibelle.includes("en_attente") ||
           statutLibelle.includes("initialisation")) {
    statut = "pending";
  } 
  // Vérifier les statuts en cours
  else if (statutCode.includes("processing") || statutCode.includes("en_cours") ||
           statutLibelle.includes("en cours") || statutLibelle.includes("en_cours")) {
    statut = "processing";
  } 
  // Pour les statuts intermédiaires (statut_entree_agent_2, statut_entree_agent_3, etc.)
  // qui ne sont ni le premier ni le dernier, considérer comme "processing"
  else if (statutCode.includes("statut_") || statutCode.match(/statut_\w+_\d+/)) {
    statut = "processing";
  } 
  // Par défaut, si aucun pattern ne correspond, utiliser "processing" au lieu de "failed"
  else {
    statut = "processing";
  }
    */
  statut = processus.statut.libelle;
  // Formater les dates
  const formatDate = (dateString: string | null | undefined) => {
    if (!dateString) {
      return '';
    }
    try {
      // Gérer les formats ISO (avec ou sans timezone)
      const date = new Date(dateString);
      
      // Vérifier si la date est valide
      if (isNaN(date.getTime())) {
        console.warn('Date invalide:', dateString);
        return dateString;
      }
      
      return date.toLocaleDateString('fr-FR', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric' // Utiliser 'numeric' au lieu de '2-digit' pour l'année complète
      });
    } catch (error) {
      console.warn('Erreur lors du formatage de la date:', dateString, error);
      return dateString;
    }
  };

  // Calculer la date de mobilisation selon le type de processus
  // Priorité: dateMobilite du backend si disponible
  let dateMobilisation: string | null = null;
  if (processus.dateMobilite) {
    dateMobilisation = processus.dateMobilite;
  } else if (type === "entree") {
    dateMobilisation = processus.dateArrivee;
  } else if (type === "sortie") {
    dateMobilisation = processus.agent.dateSortie || null;
  } else {
    dateMobilisation = processus.dateArrivee;
  }

  return {
    id: processus.id,
    type: type,
    agent: {
      nomPrenom: `${processus.agent.prenom} ${processus.agent.nom}`,
      // Si le processus concerne un prestataire DAPPI ou un conseiller/attaché DAF, adapter le libellé de direction
      direction: getDirectionDisplay({
        direction: processus.agent.direction || processus.labelleDirectionConcernee,
        role: processus.agent.role,
      }),
    },
    statut: statut,
    dateEcheance: formatDate(processus.dateEcheance),
    dateMobilite: formatDate(dateMobilisation)
  };
}
