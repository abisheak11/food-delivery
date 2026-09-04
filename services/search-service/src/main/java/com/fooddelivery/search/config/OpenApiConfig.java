package com.fooddelivery.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI searchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Food Delivery - Search Service API")
                        .description("Microservice for searching restaurants, cuisines, dishes, and menu items.")
                        .version("1.0.0")
                        .contact(new Contact().name("Food Delivery Team").email("support@fooddelivery.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
