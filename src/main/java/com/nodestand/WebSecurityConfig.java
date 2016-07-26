package com.nodestand;

import com.nodestand.auth.StatelessAuthenticationFilter;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.social.UserIdSource;
import org.springframework.social.security.SpringSocialConfigurer;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    @Autowired
    private StatelessAuthenticationFilter statelessAuthenticationFilter;

    @Autowired
    private UserIdSource userIdSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                    .antMatchers("/", "/signin/**", "/js/**", "/css/**", "/favicon.ico", "/partials/**").permitAll()
                .and()
                // add custom authentication filter for complete stateless JWT based authentication
                .addFilterBefore(statelessAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)

                // apply the configuration from the socialConfigurer (adds the SocialAuthenticationFilter)
                .apply(new SpringSocialConfigurer().userIdSource(userIdSource));
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    protected UserService userDetailsService() {
        return userService;
    }
}