package com.nodestand;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // Keep this in sync with app.js!
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/graph/**").setViewName("index");
        registry.addViewController("/create").setViewName("index");
        registry.addViewController("/profile/**").setViewName("index");
        registry.addViewController("/history/**").setViewName("index");
        registry.addViewController("/settings").setViewName("index");
    }
}