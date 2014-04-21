package com.github.wdxzs1985.reitaisai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.wdxzs1985.html.Crawler;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Value("${url:http://reitaisai.com/circlelist/circlename/}")
    private String url;

    @Value("${htmlfile:circle.html}")
    private String htmlfile;

    @Value("${csvfile:circle.csv}")
    private String csvfile;

    @Value("${sortByName:circlename.json}")
    private String sortByName;

    @Value("${sortByPos:circlepos.json}")
    private String sortByPos;

    @Value("${crawl:true}")
    private boolean crawl;

    private final Crawler crawler = new Crawler();

    private final Log log = LogFactory.getLog(this.getClass());

    private final Pattern TABLE_PATTERN = Pattern.compile("<h3 id=\"(.)\">.</h3><table class=\"circlelist\">(.*?)</table>");
    private final Pattern ITEM_PATTERN = Pattern.compile("<tr class=\".*?\"><td>(.*?)</td><td>(.*?)</td><td>(.)([0-9]{2})([ab])</td></tr>");

    @Override
    public void run(String... arg0) throws Exception {
        String html = this.getHtml();
        this.parseHtml(html);

    }

    private String getHtml() throws Exception {
        File file = new File(this.htmlfile);
        String html = null;
        if (this.crawl) {
            html = this.crawler.getHtml(this.url);
            FileUtils.write(file, html);
        } else {
            html = FileUtils.readFileToString(file);
        }
        return html.replaceAll("\\r\\n[\\t\\s]*|\\r[\\t\\s]*|\\n[\\t\\s]*", "");
    }

    private void parseHtml(String html) throws IOException {
        List<String> lines = new ArrayList<String>();
        this.parseTable(html, lines);
        this.outputCSV(lines);
        // this.outputSortByName(lines);
        // this.outputSortByPos(lines);
    }

    private void parseTable(String html, List<String> lines) {
        Matcher matcher = this.TABLE_PATTERN.matcher(html);
        while (matcher.find()) {
            String alpha = matcher.group(1);
            String table = matcher.group(2);
            this.parseRow(alpha, table, lines);
        }
    }

    private void parseRow(String alpha, String table, List<String> lines) {
        Matcher matcher = this.ITEM_PATTERN.matcher(table);
        while (matcher.find()) {
            String circleName = matcher.group(1);
            String penName = matcher.group(2);
            String position1 = matcher.group(3);
            String position2 = matcher.group(4);
            String position3 = matcher.group(5);
            lines.add(String.format("%s,%s,%s,%s,%s,%s,,",
                                    alpha,
                                    circleName,
                                    penName,
                                    position1,
                                    position2,
                                    position3));
        }
    }

    private void outputCSV(List<String> lines) throws IOException {
        FileUtils.writeLines(new File(this.csvfile), lines);
    }

    private void outputSortByName(List<JSONObject> lines) throws IOException {
        String key = null;
        JSONObject data = new JSONObject();
        for (JSONObject circle : lines) {
            key = circle.getString("alpha");
            JSONArray group = data.optJSONArray(key);
            if (group == null) {
                group = new JSONArray();
            }
            group.add(circle);
            data.put(key, group);
        }

        FileUtils.write(new File(this.sortByName),
                        "var data = " + data.toString());
    }

    private void outputSortByPos(List<JSONObject> lines) throws IOException {

        Collections.sort(lines, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                String position11 = o1.getString("position1");
                String position21 = o2.getString("position1");
                if (position11.charAt(0) != position21.charAt(0)) {
                    return position11.charAt(0) - position21.charAt(0);
                }

                String position12 = o1.getString("position2");
                String position22 = o2.getString("position2");
                if (Integer.parseInt(position12) != Integer.parseInt(position22)) {
                    return Integer.parseInt(position12) - Integer.parseInt(position22);
                }

                String position13 = o1.getString("position3");
                String position23 = o2.getString("position3");
                if (position13.charAt(0) != position23.charAt(0)) {
                    return position13.charAt(0) - position23.charAt(0);
                }

                return 0;
            }
        });

        String key = null;
        JSONObject data = new JSONObject();
        for (JSONObject circle : lines) {
            key = circle.getString("position1");
            JSONArray group = data.optJSONArray(key);
            if (group == null) {
                group = new JSONArray();
            }
            group.add(circle);
            data.put(key, group);
        }

        FileUtils.write(new File(this.sortByPos),
                        "var data = " + data.toString());
    }
}
