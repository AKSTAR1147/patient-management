package ink.starblog.apigateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object>{

    //creating a webclient that can send a request to another service like an browser and get th response
    private final WebClient webClient;

    //webclinet DI with passing the URL of the path of request
    //the path of the service can vary over the deployment environment so we are passing that value throgh the ENV VARS
    public JwtValidationGatewayFilterFactory(@Value("${auth.service.url}") String authServiceUrl){
        this.webClient=WebClient.create(authServiceUrl);
    }

    //creating gateway filter
    //this will first send the request to auth service over the gateway
    //and check if the token is valid by sending request to auth by the webClient
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->{
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if(token == null || !token.startsWith("Bearer ")){
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }


            //if the token is not null
            //then sent to the validateendpint of the auth-service
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()//retrieve the response
                    .toBodilessEntity()//no body in the response
                    .then(chain.filter(exchange));//continue the request chain either to the destination url or the next filter


        };
    }
}
