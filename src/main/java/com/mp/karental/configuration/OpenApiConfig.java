package com.mp.karental.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
//                contact = @Contact(
//                        name = "Karental system",
//                        email = "karental@gmail.com"
////                        url = "http: "
//                ),
                title = "OpenApi specification - Karental",
                description = "OpenApi documentation for Karental system",
                version = "1.0"
//                ,
//                license = @License(
//                        name = "License name"
//                )
//                ,
//                termsOfService = "bla bla naof nghi ra thi viet vao"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080/karental"
                )
//                ,
//                @Server(
//                        description = "PROD ENV",
//                        //TODO: CHANGE WHEN DEPLOY
//                        url = "http://localhost:8080"
//                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Jwt auth descripton",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
