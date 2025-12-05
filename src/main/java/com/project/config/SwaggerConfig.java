package com.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${project.name:Backend API}")
  private String projectName;

  @Value("${project.version:1.0.0}")
  private String projectVersion;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(projectName)
                .description(projectName + " Documentation")
                .version(projectVersion));
  }
}
