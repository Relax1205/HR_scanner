    package ru.hackathon.springcourse.config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import ru.hackathon.springcourse.services.UserDetailService;

    @Configuration
    public class SecurityConfig {

        private final UserDetailService userDetailService;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;

        public SecurityConfig(UserDetailService userDetailService, BCryptPasswordEncoder bCryptPasswordEncoder) {
            this.userDetailService = userDetailService;
            this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailService);
            authProvider.setPasswordEncoder(bCryptPasswordEncoder);
            return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authenticationProvider(authenticationProvider())
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/register", "/logo.png", "/favicon.ico")
                            .permitAll()
                            .requestMatchers("/admin/**").hasRole("admin")
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/login")
                            .loginProcessingUrl("/process_login")
                            .usernameParameter("email")
                            .passwordParameter("password")
                            .defaultSuccessUrl("/go", true)
                            .failureUrl("/login?error")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl("/login?logout")
                            .permitAll()
                    );
            return http.build();
        }

    }
