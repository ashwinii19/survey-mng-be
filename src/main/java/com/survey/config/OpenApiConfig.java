package com.survey.config;

import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

  @Bean
  public OpenAPI baseOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Survey Tool API")
            .version("v1.0.0")
            .description("Employee and Department Management System")
            .contact(new Contact().name("Survey Tool Team").email("team@surveytool.com")))
        .servers(List.of(new Server().url("http://localhost:8080").description("Local Server")))
        .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
            new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("survey-tool")
        .packagesToScan("com.survey.controller") // 👈 make sure this matches your controller package
        .pathsToMatch("/**")
        .build();
  }
}
