package com.nodestand;

import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    Md5PasswordEncoder passwordEncoder;

    @Autowired
    SaltSource saltSource;

    @Autowired
    NodeUserDetailsService userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                .antMatchers("/", "/js/*", "/css/*", "/partials/*", "/list", "/generateTestData", "/graph", "/detail", "/nodeMenu").permitAll()
                .anyRequest().authenticated();
        http
                .formLogin()
                .and()
                .logout()
                .permitAll();
    }


    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(userRepository);
        dao.setPasswordEncoder(passwordEncoder);
        dao.setSaltSource(saltSource);
        return dao;
    }

    @Bean
    public ProviderManager providerManager() {
        List<AuthenticationProvider> list = new ArrayList<AuthenticationProvider>();
        list.add(daoAuthenticationProvider());
        return new ProviderManager(list);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.authenticationProvider(daoAuthenticationProvider());
        auth.userDetailsService(userRepository);
    }

//    @Configuration
//    protected static class AuthenticationConfiguration extends
//            GlobalAuthenticationConfigurerAdapter {
//
//        @Autowired
//        NodeUserDetailsService userRepository;
//
//        @Autowired
//        DaoAuthenticationProvider daoAuthenticationProvider;
//
//        @Override
//        public void init(AuthenticationManagerBuilder auth) throws Exception {
//
//            auth.userDetailsService(userRepository);
//
//            auth.authenticationProvider(daoAuthenticationProvider);
//        }
//
//    }

}