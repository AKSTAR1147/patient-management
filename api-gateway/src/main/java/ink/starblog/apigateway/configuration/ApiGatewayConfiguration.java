package ink.starblog.apigateway.configuration;

import ink.starblog.apigateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {
    private final JwtValidationGatewayFilterFactory jwtFilter;

    public ApiGatewayConfiguration(JwtValidationGatewayFilterFactory jwtValidationGatewayFilterFactory){
        this.jwtFilter=jwtValidationGatewayFilterFactory;
    }
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder){


        return builder.routes()
                .route(p -> p.path("/auth-service/**")
                        .filters(f -> f
                                .stripPrefix(1))
                        .uri("lb://auth-service"))

                //patinet service path
                .route(p -> p.path("/patient-service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtFilter.apply(new Object()))
                        )
                        .uri("lb://patient-service"))

                //billing service path
                .route(p -> p.path("/billing-service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtFilter.apply(new Object()))
                        )
                        .uri("lb://billing-service"))

                //Analytics service path
                .route(p -> p.path("/analytics-service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(jwtFilter.apply(new Object()))
                        )
                        .uri("lb://analytics-service"))
                .build();
    }
}
