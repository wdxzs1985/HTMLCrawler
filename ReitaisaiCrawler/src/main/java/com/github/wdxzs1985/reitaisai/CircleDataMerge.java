package com.github.wdxzs1985.reitaisai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.github.wdxzs1985.html.Crawler;

@Component
public class CircleDataMerge implements CommandLineRunner {

    @Value("${CircleListByName.csvfile:circlename.csv}")
    private String circlenameCsv;

    @Value("${CircleListByBlock123.csvfile:block123.csv}")
    private String block123Csv;

    @Value("${CircleListByBlock456.csvfile:block456.csv}")
    private String block456Csv;

    @Value("${CircleDataMerge.csvfile:circleweb.csv}")
    private String mergedCsv;

    @Value("${CircleDataMerge.pixiv:false}")
    private boolean pixiv;

    private final Log log = LogFactory.getLog(this.getClass());

    private final Crawler crawler = new Crawler();

    private static final Pattern AVATAR_PATTERN = Pattern.compile("<img src=\"(http://i[0-9]+\\.pixiv.net/img[0-9]+/profile/.*?/[0-9]+\\.(jpe?g|png|gif))\" alt=\"\" class=\"user-image\">");

    @Override
    public void run(String... arg0) throws Exception {
        List<String> merge = new ArrayList<String>();

        List<String> block123Lines = FileUtils.readLines(new File(this.block123Csv));
        List<String> block456Lines = FileUtils.readLines(new File(this.block456Csv));

        this.importCircle(merge, block123Lines);
        this.importCircle(merge, block456Lines);

        this.output(merge);
    }

    private void importCircle(List<String> merge, List<String> lines) throws Exception {
        for (String line : lines) {
            String[] sa = Application.splitCsv(line);
            String name = sa[1];
            String web = sa[6];
            String pixivNo = sa[7];
            String avatar = "";
            if (StringUtils.isNotBlank(web) || StringUtils.isNotBlank(pixivNo)) {
                if (this.pixiv && StringUtils.isNotBlank(pixivNo)) {
                    avatar = this.findAvatar(pixivNo);
                }
                String newLine = String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
                                               name,
                                               web,
                                               pixivNo,
                                               avatar);
                merge.add(newLine);
            }
        }
    }

    private String findAvatar(String pixivNo) throws Exception {
        String url = String.format("http://www.pixiv.net/member.php?id=%s",
                                   pixivNo);
        Thread.sleep(1000);
        String html = this.crawler.getHtml(url);
        html = Application.replaceReturn(html);

        Matcher matcher = AVATAR_PATTERN.matcher(html);
        if (matcher.find()) {
            this.log.info(matcher.group(1));
            return matcher.group(1);
        } else {
            this.log.info(html);
        }
        return null;
    }

    private void output(List<String> merge) throws IOException {
        FileUtils.writeLines(new File(this.mergedCsv), merge);
    }

}
