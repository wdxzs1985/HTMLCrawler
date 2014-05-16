package com.github.wdxzs1985.pixiv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@PropertySources(value = { @PropertySource("file:application.txt") })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
