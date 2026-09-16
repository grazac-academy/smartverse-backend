package org.smartvert.smartvert.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("SmartVert API")
                                                .version("1.0.0")
                                                .description("Backend API for the SmartVert solar sizing and inverter estimation engine."))
                                .servers(List.of(
                                                new Server().url("https://smart-vert-app.onrender.com")
                                                                .description("Production Server"),
                                        new Server().url("https://smartvert-1mu7kn9j.b4a.run")
                                                                .description("Production Server"),
                                                new Server().url("http://localhost:8080")
                                                                .description("Local Development Server")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .name("bearerAuth")
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT Bearer access token authentication")));
        }
}
