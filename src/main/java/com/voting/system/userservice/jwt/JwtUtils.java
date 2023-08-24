package com.voting.system.userservice.jwt;

import com.voting.system.userservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

  private static final String SECRET_KEY = "789456123";
  private static final long EXPIRATION_TIME = 3600000;
  private static SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  public static String generateJwtToken(User user) {
    JwtBuilder jwtBuilder = Jwts.builder()
            .setSubject(Long.toString(user.getUserId()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, key);

    return jwtBuilder.compact();
  }

  public String getUserIdFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public long getUserIdFromTokenClaims(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(key) // The same key you used for signing
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.get("userId", Long.class);
  }


  //retrieve expiration date from jwt token
  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  //for retrieveing any information from token we will need the secret key
  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
  }


  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = (getUserIdFromToken(token));
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
