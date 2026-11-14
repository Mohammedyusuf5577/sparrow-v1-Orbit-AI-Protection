package com.orbit.config;

import com.orbit.util.AppUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("ORBIT API")
                                                .description("AI Based Organizational Risk & Intelligence Platform - Admin Module API")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("ORBIT Team")
                                                                .email("admin@orbit.com")))
                                .components(new Components()
                                                .addSecuritySchemes(AppUtils.Constants.SECURITY_SCHEME_NAME,
                                                                new SecurityScheme()
                                                                                .name(AppUtils.Constants.SECURITY_SCHEME_NAME)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList(AppUtils.Constants.SECURITY_SCHEME_NAME))
                                .externalDocs(new ExternalDocumentation()
                                                .description("ORBIT Documentation"));
        }

        @Bean
        public GroupedOpenApi orbitApi() {
                return GroupedOpenApi.builder()
                                .group("orbit-api")
                                .pathsToMatch("/api/**")
                                .build();
        }
}

