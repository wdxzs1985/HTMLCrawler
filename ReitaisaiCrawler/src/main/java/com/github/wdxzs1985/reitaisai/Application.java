package com.github.wdxzs1985.reitaisai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.wdxzs1985.html.CommonHttpClient;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36";
    private static final CommonHttpClient HTTP = new CommonHttpClient(USER_AGENT);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public static final String getHtml(String url) {
        String html = HTTP.getForHtml(url);
        HTTP.setReferer(url);
        return html;
    }

    public static final byte[] getByteArray(String url) throws IOException {
        HttpResponse response = HTTP.get(url);
        return EntityUtils.toByteArray(response.getEntity());

    }

    public static final String[] splitCsv(String line) {
        List<String> splited = new ArrayList<String>();
        StringBuilder strB = new StringBuilder();
        char strChar = 0;
        boolean isString = false;

        for (int i = 0; i < line.length(); i++) {
            strChar = line.charAt(i);
            if (strChar == "\"".charAt(0)) {
                isString = isString ? false : true;
            } else if (!isString && strChar == ",".charAt(0)) {
                splited.add(strB.toString());
                strB = new StringBuilder();
            } else {
                strB.append(strChar);
            }
        }
        splited.add(strB.toString());
        return splited.toArray(new String[] {});
    }

    public static final String replaceReturn(String text) {
        return text.replaceAll("\\r\\n[\\t\\s]*|\\r[\\t\\s]*|\\n[\\t\\s]*", "");
    }
}
