package ink.starblog.authservice.Util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;

    //initializing the secret key
    //as this key is very vulnerable, need to pass it using ENV VARS
    public JwtUtil(@Value("${jwt.secret}") String secret){
        //in the .props or the ENV VARS  we will pass it as base64 encoded
        //first convert in bytes array
        byte[] keybites = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));

        this.secretKey = Keys.hmacShaKeyFor(keybites);
    }
    //JWT token creation by embedding the user info such as email and role
    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 30 mins
                .signWith(secretKey)
                .compact();


    }

    public void validateToken(String token){
        try{
            Jwts.parser().verifyWith((SecretKey)secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT");
        }
    }
}
