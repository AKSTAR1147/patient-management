package ink.starblog.authservice.controller;


import ink.starblog.authservice.dto.LoginResponseDto;
import ink.starblog.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@RestController
public class AuthController {
    //Record to use in place of DTO
    public record LoginRequest(
            @NotBlank(message = "This field cannot be blank")
            @Email(message = "Email should be valid one")
            String email,

            @NotBlank(message = "password is required")
            @Size(min = 8, message = "password should be at lest 8 char long")
            String password
    ){}

    private AuthService authService;

    AuthController(AuthService authService){
        this.authService=authService;
    }

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest request){
        Optional<String> tokenOptinal = authService.authenticate(request);

        if(tokenOptinal.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = tokenOptinal.get();
        return ResponseEntity.ok(new LoginResponseDto(token));
    }



    @GetMapping("/validate")
    //taking the Authorization string from the  request header named  Authorization
    public  ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader){

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return authService.validateToken(authHeader.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
