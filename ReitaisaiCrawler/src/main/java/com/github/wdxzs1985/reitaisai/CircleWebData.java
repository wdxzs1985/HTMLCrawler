package com.github.wdxzs1985.reitaisai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CircleWebData implements CommandLineRunner {

    @Value("${CircleListByBlock123.csvfile:block123.csv}")
    private String block123Csv;

    @Value("${CircleListByBlock456.csvfile:block456.csv}")
    private String block456Csv;

    @Value("${CircleWebData.csvfile:web.csv}")
    private String csvfile;

    @Value("${CircleWebData.jsonfile:web.json}")
    private String jsonfile;

    @Value("${CircleWebData.csv:true}")
    private boolean csv;

    private final Log log = LogFactory.getLog(this.getClass());

    private static final Pattern SOUNDCLOUD_PATTERN = Pattern.compile("<iframe width=\"100%\" height=\"166\" scrolling=\"no\" frameborder=\"no\" src=\"(https://w\\.soundcloud\\.com/player/\\?url=https%3A//api\\.soundcloud\\.com/tracks/\\d+&.*?)\"></iframe>");

    @Override
    public void run(String... arg0) throws Exception {
        this.mergeCsv();
        this.output();
    }

    private void mergeCsv() throws Exception {
        if (this.csv) {
            List<String> merge = new ArrayList<String>();
            // List<String> block123Lines = FileUtils.readLines(new
            // File(this.block123Csv));
            List<String> block456Lines = FileUtils.readLines(new File(this.block456Csv));
            // this.importCircle(merge, block123Lines);
            this.importCircle(merge, block456Lines);
            this.outputCsv(merge);
        }
    }

    private void importCircle(List<String> merge, List<String> lines) throws Exception {
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            String name = sa[1];
            String web = sa[6];
            if (StringUtils.isNotBlank(web)) {
                String newLine = null;
                try {
                    String html = Application.getHtml(web);
                    html = StringEscapeUtils.unescapeHtml4(html);
                    html = Application.replaceReturn(html);
                    String soundCloud = this.findSoundCloud(html);
                    // text = this.findInfo(web);
                    newLine = String.format("\"%s\",\"%s\",\"%s\"",
                                            name,
                                            web,
                                            soundCloud);
                } catch (Exception e) {
                    newLine = String.format("\"%s\",\"%s\",\"%s\"",
                                            name,
                                            "",
                                            "");
                } finally {
                    merge.add(newLine);
                }
            }
        }
    }

    private String findSoundCloud(String html) throws Exception {
        Thread.sleep(2000);
        Matcher matcher = SOUNDCLOUD_PATTERN.matcher(html);
        if (matcher.find()) {
            String soundCloud = matcher.group(1);
            this.log.info(html);
            return soundCloud;
        } else {
        }
        return null;
    }

    private void outputCsv(List<String> merge) throws IOException {
        FileUtils.writeLines(new File(this.csvfile), merge);
    }

    private void output() throws IOException {
        List<String> lines = FileUtils.readLines(new File(this.csvfile));
        JSONObject webMap = new JSONObject();
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            String name = sa[0];
            String web = sa[1];
            String text = sa[2];

            JSONObject circle = new JSONObject();
            circle.put("name", name);
            circle.put("web", web);
            circle.put("text", StringEscapeUtils.unescapeCsv(text));

            webMap.put(name, circle);
        }

        FileUtils.write(new File(this.jsonfile),
                        "var webData = " + webMap.toString());
    }
}
