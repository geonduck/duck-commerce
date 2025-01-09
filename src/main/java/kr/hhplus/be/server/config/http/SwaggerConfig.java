package kr.hhplus.be.server.config.http;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Sample API",
                description = "Swagger 정리.",
                version = "v1"))
@RequiredArgsConstructor

@Configuration
public class SwaggerConfig{

    @Bean
    public GroupedOpenApi SampleUserOpenApi() {
        String[] paths = {"/api/**"};

        return GroupedOpenApi.builder()
                .group("kr.hhplus.be.server.interfaces")
                .pathsToMatch(paths)
                .build();
    }
}