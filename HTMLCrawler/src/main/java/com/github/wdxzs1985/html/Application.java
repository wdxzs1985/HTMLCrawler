package com.github.wdxzs1985.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Value("${url}")
    private String url;

    @Autowired
    private final Crawler crawler = null;

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void run(String... arg0) throws Exception {
        String html = this.crawler.getHtml(this.url);
        this.log.info(html);
    }

}
