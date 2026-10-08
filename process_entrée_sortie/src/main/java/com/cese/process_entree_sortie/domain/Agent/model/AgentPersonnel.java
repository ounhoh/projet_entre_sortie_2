package com.cese.process_entree_sortie.domain.Agent.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import com.cese.process_entree_sortie.domain.utils.error.*;

import java.util.*;

public record AgentPersonnel (UUID id, String nom,  String prenom, String email,
                              Role role, EtatAgent etatAgent,
                              List<AgentDirection> agentDirections,
                              List<AgentAffectation> agentAffectations,
                              AgentMaterielEtDroit agentMaterielEtDroit)
{
    public AgentPersonnel {
        Objects.requireNonNull(nom);
        Objects.requireNonNull(prenom);
        Objects.requireNonNull(role);
        Objects.requireNonNull(etatAgent);
        validerNom(nom);
        validerPrenom(prenom);
    }

    public AgentPersonnel(UUID id, String nom, String prenom,Role role,List<AgentDirection> agentDirections,
                          List<AgentAffectation> agentAffectations, AgentMaterielEtDroit agentMaterielEtDroit)
    {
        this(id,nom,prenom,creationEmail(nom,prenom,"lecese.fr"),role,EtatAgent.entree,agentDirections,
                agentAffectations,agentMaterielEtDroit);
    }
    private static String creationEmail(String nom, String prenom, String domain)
    {
        Objects.requireNonNull(nom);
        Objects.requireNonNull(prenom);
        Objects.requireNonNull(domain);
        validerNom(prenom);
        validerNom(nom);
       return prenom.toLowerCase() + "." + nom.toLowerCase() + "@" + domain.toLowerCase();
    }
    private static void validerPrenom(String prenom)
    {
        if (prenom.isBlank())
        {
            throw  new PrenomInvalideException("Prenom trop court");
        }
    }

    private static void validerNom(String nom)
    {
        if (nom.isBlank())
        {
            throw  new NomInvalideException("Nom trop court");
        }
    }


    private static void validerEmail(String email, String domain)
    {
        Objects.requireNonNull(email);
        Objects.requireNonNull(domain);
       if (email.isBlank())
       {

           throw  new EmailInvalideException("email trop court");
       }
       if (!(email.contains("@" + domain)))
       {
           throw  new EmailInvalideException("l'email: "+ email + "ne respecte pas le domaine: " + domain);
       }
    }

    public AgentPersonnel changerAdresseMail(String newEmail)
    {
        validerEmail(email,"lecese.fr");
        return new AgentPersonnel(id,nom,prenom,newEmail,role,etatAgent,agentDirections,agentAffectations,agentMaterielEtDroit);
    }
    public Optional<AgentMaterielEtDroit> getOptionalAgentMaterielEtDroit()
    {
        return Optional.ofNullable(agentMaterielEtDroit);
    }
    public  AgentPersonnel changerRole(Role newRole)
    {
       return new AgentPersonnel(id,nom,prenom,email,newRole,etatAgent,agentDirections,agentAffectations,agentMaterielEtDroit);
    }
    
    public AgentPersonnel changerEtatAgent(EtatAgent newEtatAgent)
    {
        String nextEmail = email;
        if (newEtatAgent == EtatAgent.ancien && nextEmail != null && !nextEmail.contains("#ancien#")) {
            nextEmail = nextEmail + "#ancien#" + id;
        }
        return new AgentPersonnel(id,nom,prenom,nextEmail,role,newEtatAgent,agentDirections,agentAffectations,agentMaterielEtDroit);
    }

    public AgentPersonnel changerAgentResponsable(UUID idAgentResponsable)
    {
        List<AgentAffectation> agentAffectationsCopy = new ArrayList<>(agentAffectations);
        AgentAffectation agentAffectation = agentAffectations.getFirst();
        agentAffectation = agentAffectation.changerAgentResponsable(idAgentResponsable);
        agentAffectationsCopy.add(agentAffectation);
        return new AgentPersonnel(id,nom,prenom,email,role,etatAgent,agentDirections,List.copyOf(agentAffectationsCopy),agentMaterielEtDroit);
    }

    public void changerDirection(AgentDirection newAgentDirection, AgentAffectation newAgentAffectation)
    {
        if (newAgentDirection.directionId().equals(agentDirections.getFirst().directionId()))
        {
            throw new AgentDirectionInvalieException("AgentDirection");
        }
        agentDirections.addFirst(newAgentDirection);
        changerAffectation(newAgentAffectation);

    }

    public void changerAffectation(AgentAffectation newAgentAffectation)
    {
        agentAffectations.addFirst(newAgentAffectation);
    }

    public AgentPersonnel changerMaterielEtDroit(AgentMaterielEtDroit newAgentMaterielEtDroit)
    {
       return new AgentPersonnel(id,nom,prenom,email,role,etatAgent,agentDirections,agentAffectations,agentMaterielEtDroit);
    }
}