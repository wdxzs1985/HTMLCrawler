package com.github.wdxzs1985.reitaisai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.github.wdxzs1985.html.Crawler;

@Component
public class CircleListByName implements CommandLineRunner {

    @Value("${CircleListByName.url:http://reitaisai.com/circlelist/circlename/}")
    private String url;

    @Value("${CircleListByName.html:true}")
    private boolean html;

    @Value("${CircleListByName.htmlfile:circle.html}")
    private String htmlfile;

    @Value("${CircleListByName.csv:true}")
    private boolean csv;

    @Value("${CircleListByName.csvfile:circle.csv}")
    private String csvfile;

    @Value("${CircleListByName..jsonfile:circlename.json}")
    private String jsonfile;

    private final Crawler crawler = new Crawler();

    private final Log log = LogFactory.getLog(this.getClass());

    private final Pattern TABLE_PATTERN = Pattern.compile("<h3 id=\"(.)\">.</h3><table class=\"circlelist\">(.*?)</table>");
    private final Pattern ITEM_PATTERN = Pattern.compile("<tr class=\".*?\"><td>(.*?)</td><td>(.*?)</td><td>(.)([0-9]{2})([ab])</td></tr>");

    @Override
    public void run(String... arg0) throws Exception {
        this.parseHtml();
        this.parseCsv();
    }

    private String getHtml() throws Exception {
        File file = new File(this.htmlfile);
        String html = null;
        if (this.html) {
            html = this.crawler.getHtml(this.url);
            FileUtils.write(file, html);
        } else {
            html = FileUtils.readFileToString(file);
        }
        return Application.replaceReturn(html);
    }

    private void parseHtml() throws Exception {
        List<String> lines = new ArrayList<String>();
        String html = this.getHtml();
        this.parseTable(html, lines);
        this.outputCSV(lines);
    }

    private void parseTable(String html, List<String> lines) {
        Matcher matcher = this.TABLE_PATTERN.matcher(html);
        while (matcher.find()) {
            String alpha = matcher.group(1);
            String table = matcher.group(2);
            this.parseTableRow(alpha, table, lines);
        }
    }

    private void parseTableRow(String alpha, String table, List<String> lines) {
        Matcher matcher = this.ITEM_PATTERN.matcher(table);
        while (matcher.find()) {
            String circleName = matcher.group(1);
            String penName = matcher.group(2);
            String position1 = matcher.group(3);
            String position2 = matcher.group(4);
            String position3 = matcher.group(5);
            lines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                                    alpha,
                                    StringEscapeUtils.unescapeHtml4(circleName),
                                    StringEscapeUtils.unescapeHtml4(penName),
                                    position1,
                                    position2,
                                    position3));
        }
    }

    private void outputCSV(List<String> lines) throws IOException {
        FileUtils.writeLines(new File(this.csvfile), lines);
    }

    private void parseCsv() throws IOException {
        if (this.csv) {
            List<String> lines = FileUtils.readLines(new File(this.csvfile));
            this.output(lines);
        }
    }

    private void output(List<String> lines) throws IOException {
        String key = null;
        JSONObject data = new JSONObject();
        JSONArray keymap = new JSONArray();
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            key = sa[0];
            JSONArray group = data.optJSONArray(key);
            if (group == null) {
                group = new JSONArray();
                keymap.add(key);
            }

            JSONObject circle = new JSONObject();
            circle.put("name", sa[1]);
            circle.put("penName", sa[2]);
            circle.put("position1", sa[3]);
            circle.put("position2", sa[4]);
            circle.put("position3", sa[5]);

            group.add(circle);
            data.put(key, group);
        }
        data.put("keymap", keymap);

        FileUtils.write(new File(this.jsonfile),
                        "var data = " + data.toString());
    }

}
