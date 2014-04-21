package com.github.wdxzs1985.html;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Crawler implements CommandLineRunner {

    @Value("${url}")
    private String url;

    @Override
    public void run(String... arg0) throws Exception {
        System.out.println("Crawler start");
        if (StringUtils.hasText(this.url)) {
            System.out.println(this.url);
            HttpClient httpc = HttpClients.createMinimal();
            final HttpGet httpget = new HttpGet(this.url);
            HttpResponse response = httpc.execute(httpget);
            String html = EntityUtils.toString(response.getEntity(), "utf8");
            System.out.println(html);
        }
    }

}
