package com.cese.process_entree_sortie.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration pour enregistrer l'interceptor qui ajoute charset=utf-8 aux réponses JSON
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CharsetInterceptor charsetInterceptor;

    public WebMvcConfig(CharsetInterceptor charsetInterceptor) {
        this.charsetInterceptor = charsetInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(charsetInterceptor);
    }
}

