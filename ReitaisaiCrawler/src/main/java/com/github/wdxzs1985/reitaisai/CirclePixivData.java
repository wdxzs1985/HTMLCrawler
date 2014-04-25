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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CirclePixivData implements CommandLineRunner {

    @Value("${CircleListByBlock123.csvfile:block123.csv}")
    private String block123Csv;

    @Value("${CircleListByBlock456.csvfile:block456.csv}")
    private String block456Csv;

    @Value("${CirclePixivData.csvfile:pixiv.csv}")
    private String csvfile;

    @Value("${CirclePixivData.jsonfile:pixiv.json}")
    private String jsonfile;

    @Value("${CirclePixivData.csv:false}")
    private boolean csv;

    private final Log log = LogFactory.getLog(this.getClass());

    private static final Pattern AVATAR_PATTERN = Pattern.compile("<img src=\"(http://i[0-9]+\\.pixiv.net/img[0-9]+/profile/.*?/[0-9]+\\.(jpe?g|png|gif))\" alt=\"\" class=\"user-image\">");

    @Override
    public void run(String... arg0) throws Exception {
        this.mergeCsv();
        this.output();
    }

    private void mergeCsv() throws Exception {
        if (this.csv) {
            List<String> merge = new ArrayList<String>();
            List<String> block123Lines = FileUtils.readLines(new File(this.block123Csv));
            List<String> block456Lines = FileUtils.readLines(new File(this.block456Csv));
            this.importCircle(merge, block123Lines);
            this.importCircle(merge, block456Lines);
            this.outputCsv(merge);
        }
    }

    private void importCircle(List<String> merge, List<String> lines) throws Exception {
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            String name = sa[1];
            String pixivNo = sa[7];
            if (StringUtils.isNotBlank(pixivNo)) {
                String avatar = this.findAvatar(pixivNo);
                String newLine = String.format("\"%s\",\"%s\",\"%s\"",
                                               name,
                                               pixivNo,
                                               avatar);
                merge.add(newLine);
            }
        }
    }

    private String findAvatar(String pixivNo) throws Exception {
        Thread.sleep(2000);
        String url = String.format("http://www.pixiv.net/member.php?id=%s",
                                   pixivNo);
        String html = Application.getHtml(url);
        html = Application.replaceReturn(html);

        Matcher matcher = AVATAR_PATTERN.matcher(html);
        if (matcher.find()) {
            String avatar = matcher.group(1);
            String ext = matcher.group(2);
            String filename = String.format("pixiv/%s.%s", pixivNo, ext);
            File file = new File("www/" + filename);
            byte[] data = Application.getByteArray(avatar);
            FileUtils.writeByteArrayToFile(file, data);
            return filename;
        } else {
            this.log.info(html);
        }
        return null;
    }

    private void outputCsv(List<String> merge) throws IOException {
        FileUtils.writeLines(new File(this.csvfile), merge);
    }

    private void output() throws IOException {
        List<String> lines = FileUtils.readLines(new File(this.csvfile));
        JSONObject pixivMap = new JSONObject();
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            String name = sa[0];
            String pixiv = sa[1];
            String avatar = sa[2];

            JSONObject circle = new JSONObject();
            circle.put("name", name);
            circle.put("pixiv", pixiv);
            circle.put("avatar", avatar);

            pixivMap.put(name, circle);
        }

        FileUtils.write(new File(this.jsonfile),
                        "var pixivData = " + pixivMap.toString());
    }
}
