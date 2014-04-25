package com.github.wdxzs1985.pixiv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PixivCrawler implements CommandLineRunner {

    private final static Pattern NAME_PATTERN = Pattern.compile("<h1 class=\"name\">(.*?)</h1>");
    private final static Pattern ILLUST_PATTERN = Pattern.compile("<a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\">");

    private final static Pattern ILLUST_TITLE_PATTERN = Pattern.compile("<h1 class=\"title\">([^<>]+)</h1>");
    private final static Pattern ILLUST_CAPTION_PATTERN = Pattern.compile("<div id=\"caption_long\" class=\"caption\" style=\"display: block;\">(.*?)</div>");
    private final static Pattern ILLUST_IMAGE_PATTERN = Pattern.compile("<img src=\"(http://i[\\d]\\.pixiv\\.net/img[0-9]+/img/.*?/\\d+_m.png)\" alt=\"(.*?)\" title=\"(.*?)\" border=\"0\" />");
    private final static Pattern ILLUST_TAG_PATTERN = Pattern.compile("<li><a href=\"/tags.php\\?tag=.*?\" class=\"tag-icon\">c</a><a href=\"/tags.php\\?tag=.*?\">(.*?)</a></li>");

    private final Log log = LogFactory.getLog(this.getClass());

    @Value("${pixiv}")
    private String pixiv;

    @Override
    public void run(String... arg0) throws Exception {
        String url = String.format("http://www.pixiv.net/member.php?id=%s",
                                   this.pixiv);
        String html = Application.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = Application.replaceReturn(html);

        this.findName(html);
        this.findIllust(html);
    }

    private String findName(String html) {
        String name = null;
        Matcher matcher = null;
        if ((matcher = NAME_PATTERN.matcher(html)).find()) {
            name = matcher.group(1);
            this.log.info(name);
        }
        return name;
    }

    private void findIllust(String html) throws InterruptedException {
        Matcher matcher = null;
        matcher = ILLUST_PATTERN.matcher(html);
        while (matcher.find()) {
            String illustId = matcher.group(1);
            this.getIllust(illustId);
        }
    }

    private void getIllust(String illustId) throws InterruptedException {
        Thread.sleep(1000);
        String url = String.format("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=%s",
                                   illustId);
        String html = Application.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = Application.replaceReturn(html);

        this.findIllustTitle(html);
        this.findIllustCaption(html);
        this.findIllustImage(html);
        this.findIllustTags(html);
    }

    private String findIllustTitle(String html) {
        String title = null;
        Matcher matcher = ILLUST_TITLE_PATTERN.matcher(html);
        if (matcher.find()) {
            title = matcher.group(1);
            this.log.info(title);
        }
        return title;
    }

    private String findIllustCaption(String html) {
        String caption = null;
        Matcher matcher = ILLUST_CAPTION_PATTERN.matcher(html);
        while (matcher.find()) {
            caption = matcher.group(1);
            caption = caption.replaceAll("<a href=\"/jump.php\\?.*?\" target=\"_blank\">(https?://.*?)</a>",
                                         "<a href=\"$1\" target=\"_blank\">$1</a>");
            this.log.info(caption);
        }
        return caption;
    }

    private String findIllustImage(String html) {
        String image = null;
        Matcher matcher = ILLUST_IMAGE_PATTERN.matcher(html);
        while (matcher.find()) {
            image = matcher.group(1);
            this.log.info(image);
        }
        return image;
    }

    private void findIllustTags(String html) {
        Matcher matcher = null;
        matcher = ILLUST_TAG_PATTERN.matcher(html);
        while (matcher.find()) {
            String tag = matcher.group(1);
            this.log.info(tag);
        }
    }

}
