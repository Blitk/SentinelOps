package com.sentinelops.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*");
    }

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> resolvers) {

        PageableHandlerMethodArgumentResolver resolver =
                new PageableHandlerMethodArgumentResolver();

        resolver.setMaxPageSize(100);
        resolver.setOneIndexedParameters(false);

        resolvers.add(resolver);
    }
}