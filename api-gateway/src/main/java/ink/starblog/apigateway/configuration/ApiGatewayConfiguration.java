package ink.starblog.apigateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder){

        return builder.routes()
                .route(p -> p.path("/patient-service/**")
                        .uri("lb://patient-service"))
                .route(p -> p.path("/billing-service/**")
                        .uri("lb://billing-service"))
                .route(p -> p.path("/analytics-service/**")
                        .uri("lb://analytics-service"))
                .build();
    }
}
