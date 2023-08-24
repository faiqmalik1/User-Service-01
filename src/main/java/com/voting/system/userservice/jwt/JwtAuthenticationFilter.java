package com.voting.system.userservice.jwt;

import constants.Data;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


  private final JwtUtils jwtHelper;

  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      String token = null;
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(Data.AUTHORIZATION.getValue())) {
          token = cookie.getValue();
        }
      }
      String userId = null;
      if (token != null) {
        try {
          userId = this.jwtHelper.getUserIdFromToken(token);
        } catch (IllegalArgumentException e) {
          logger.info("Illegal Argument while fetching the username !!");
          e.printStackTrace();
        } catch (ExpiredJwtException e) {
          logger.info("Given jwt token is expired !!");
          e.printStackTrace();
        } catch (MalformedJwtException e) {
          logger.info("Some changed has done in token !! Invalid Token");
          e.printStackTrace();
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);
          Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
          if (validateToken) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          } else {
            logger.info("Validation fails !!");
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private static class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final String headerValue;

    public CustomHttpServletRequestWrapper(HttpServletRequest request, String headerValue) {
      super(request);
      this.headerValue = headerValue;
    }

    @Override
    public String getHeader(String name) {
      if ("Authorization".equalsIgnoreCase(name)) {
        return headerValue;
      }
      return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      Enumeration<String> headerNames = super.getHeaderNames();
      if (headerNames == null) {
        headerNames = Collections.enumeration(Collections.singleton("Authorization"));
      }
      return headerNames;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if ("Authorization".equalsIgnoreCase(name)) {
        return Collections.enumeration(Collections.singleton(headerValue));
      }
      return super.getHeaders(name);
    }
  }

}
