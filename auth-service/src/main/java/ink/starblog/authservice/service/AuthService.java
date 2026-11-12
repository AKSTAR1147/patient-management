package ink.starblog.authservice.service;

import ink.starblog.authservice.Util.JwtUtil;
import ink.starblog.authservice.controller.AuthController;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    //deps injection using constructor injection method
    AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    //method to authenticate and return authorization token if authentication completed
    public Optional<String> authenticate(AuthController.LoginRequest request){

        Optional<String> token = userService
                .findByEmail(request.email())   //finding person by email(unique)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))  //filtering user based on input password and stored password using encoding (not decoding)
                .map(u -> jwtUtil.generateToken(u.getEmail(),u.getRole()));    //if matched return the generated token embedding the mail and the role of user

        return token;

    }



    public boolean validateToken(String token){
        try{
            jwtUtil.validateToken(token);
            return true;
        }
        catch (JwtException e){
            return false;
        }
    }
}
