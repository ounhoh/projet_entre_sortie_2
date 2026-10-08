package com.cese.process_entree_sortie.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor pour ajouter automatiquement charset=utf-8 aux réponses JSON
 * Cela corrige le warning "content-type header charset value should be 'utf-8'"
 */
@Component
public class CharsetInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // Ajouter charset=utf-8 au Content-Type si c'est du JSON et que charset n'est pas déjà présent
        String contentType = response.getContentType();
        if (contentType != null && 
            contentType.startsWith("application/json") && 
            !contentType.contains("charset")) {
            response.setContentType(contentType + "; charset=utf-8");
        }
    }
}
