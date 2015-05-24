package com.nodestand;

import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .formLogin()
                    .loginPage("/signin")
                    .loginProcessingUrl("/signin/authenticate")
                    .failureUrl("/signin?param.error=bad_credentials")
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/signout"))
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                .and()
                    .authorizeRequests()
                    .antMatchers("/", "/signin/**", "/js/*", "/css/*", "/partials/*", "/list", "/generateTestData", "/nodeMenu").permitAll()
                .and()
                    .rememberMe();
    }


    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(nodeUserDetailsService);
        return dao;
    }


    @Bean
    public ProviderManager providerManager() {
        List<AuthenticationProvider> list = new ArrayList<AuthenticationProvider>();
        list.add(daoAuthenticationProvider());
        return new ProviderManager(list);
    }
}