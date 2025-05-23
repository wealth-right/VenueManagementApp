package com.venue.mgmt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JWTValidator {

    private static String signingKey="Wc2cFuYp1GyeeIJlcRkufv1OIuhOYnCSaGFXIXdZMfPKPCAn644dWJNCwPxZHzF7duQ4J3ncgM8WpKzetTl4KA==";



    public static boolean validateToken(String token) {
        try{
            if ((token != null || !token.trim().isEmpty()) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Jwts.parser()
                    .setSigningKey(signingKey.getBytes())
                    .parseClaimsJws(token).getBody();
            return true;
        }catch (Exception e){
            return false;
        }
    }



}
