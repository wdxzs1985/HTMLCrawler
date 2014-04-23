package com.github.wdxzs1985.reitaisai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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
