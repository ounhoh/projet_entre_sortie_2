package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.port.in.agent.GetAgentDiffusionApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDiffusionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDiffusion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetAgentDiffusionService implements GetAgentDiffusionApi {

    private final AgentDiffusionSpi diffusionSpi;

    public GetAgentDiffusionService(AgentDiffusionSpi diffusionSpi) {
        this.diffusionSpi = diffusionSpi;
    }

    @Override
    public List<String> getAgentDiffusion(UUID agentId) {
        return diffusionSpi.findByAgentId(agentId)
            .map(this::splitDiffusion)
            .orElse(List.of());
    }

    private List<String> splitDiffusion(AgentDiffusion diffusion) {
        String liste = diffusion.listeDiffusion();
        if (liste == null || liste.isBlank()) {
            return List.of();
        }
        String sep = diffusion.separateur();
        String separator = (sep == null || sep.isBlank()) ? "," : sep;
        return Arrays.stream(liste.split(separator))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .collect(Collectors.toList());
    }
}
