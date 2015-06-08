package com.nodestand;

import com.nodestand.auth.CsrfHeaderFilter;
import com.nodestand.auth.RestAuthenticationEntryPoint;
import com.nodestand.auth.RestAuthenticationSuccessHandler;
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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private RestAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .formLogin()
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(new SimpleUrlAuthenticationFailureHandler())
                .and()
                    .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/signout"))
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                .and()
                    .authorizeRequests()
                    .antMatchers("/", "/signin/**", "/js/**", "/css/**", "/favicon.ico", "/partials/**", "/list", "/nodeMenu", "/graph").permitAll()
                    .antMatchers("/generateTestData").hasRole("USER")
                .and()
                    .csrf()
                    .csrfTokenRepository(csrfTokenRepository())
                .and()
                    // https://spring.io/guides/tutorials/spring-security-and-angular-js/
                    .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class);
    }

    /**
     * https://spring.io/guides/tutorials/spring-security-and-angular-js/
     * @return
     */
    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }


    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(nodeUserDetailsService);
        return dao;
    }


    @Bean
    public ProviderManager providerManager() {
        List<AuthenticationProvider> list = new ArrayList<>();
        list.add(daoAuthenticationProvider());
        return new ProviderManager(list);
    }
}