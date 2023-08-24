//package com.voting.system.userservice.security;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//  @Autowired
//  private CustomUserDetailsService userDetailsService;
//
//  @Autowired
//  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//  @Bean
//  public JwtAuthenticationFilter jwtAuthenticationFilter() {
//    return new JwtAuthenticationFilter();
//  }
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//            .csrf().disable()
//            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
//            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//            .authorizeRequests()
//            .antMatchers("/login").permitAll() // Allow access to login endpoint
//            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow pre-flight requests
//            .anyRequest().authenticated(); // Require authentication for other endpoints
//
//    // Add JWT filter
//    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//  }
//
//  @Override
//  public void configure(AuthenticationManagerBuilder auth) throws Exception {
//    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//  }
//
//  @Bean
//  public BCryptPasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder();
//  }
//
//  @Override
//  @Bean
//  public AuthenticationManager authenticationManagerBean() throws Exception {
//    return super.authenticationManagerBean();
//  }
//}
//
//Step 3: User Entity and Roles
//        Create a User entity class that includes fields for user details such as username, password, and roles. Define an enum for user roles (e.g., VOTER, ADMIN, CANDIDATE).
//
//        java
//
//        import javax.persistence.*;
//        import java.util.Set;
//
//@Entity
//public class User {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  private String username;
//  private String password;
//
//  @ElementCollection(fetch = FetchType.EAGER)
//  @Enumerated(EnumType.STRING)
//  private Set<Role> roles;
//
//  // Constructors, getters, and setters
//}