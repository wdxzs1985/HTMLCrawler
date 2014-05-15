package com.github.wdxzs1985.pixiv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

public class PixivTag extends PixivBase implements CommandLineRunner {

    private final static Pattern IMAGEITEM_PATTERN = Pattern.compile("<li class=\"image-item\"><a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\" class=\"work\"><img src=\"http://i[\\d]\\.pixiv\\.net/img-inf/img/([\\d]{4})/([\\d]{2})/([\\d]{2})/([\\d]{2})/([\\d]{2})/([\\d]{2})/[\\d]+_s\\.(jpe?g|png|gif)\" class=\"_thumbnail\"><h1 class=\"title\" title=\"(.*?)\">.*?</h1></a><a href=\"/member_illust\\.php\\?id=\\d+\" class=\"user ui-profile-popup\" title=\".*?\" data-user_id=\"(\\d+)\" data-user_name=\"(.*?)\">.*?</a></li>");
    private final static Pattern NEXT_PATTERN = Pattern.compile("<a href=\"\\?word=[%A-F0-9]+&s_mode=s_tag_full&p=(\\d+)\" rel=\"next\" class=\"_button\" title=\"次へ\">");

    @Value("${word}")
    private String word;

    private int page = 1;

    @Override
    public void run(String... arg0) throws Exception {
        this.login = this.doLogin();
        this.page = 1;

        if (this.page > 0) {
            this.getPixivTag(this.word, this.page);
            Thread.sleep(2000);
        }
    }

    private void getPixivTag(String word, int page) {
        String url = String.format("http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%s&p=%d",
                                   word,
                                   page);
        String html = Application.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = Application.replaceReturn(html);
        this.find(html);
        this.findPage(html);
    }

    private void find(String html) {
        Matcher matcher = IMAGEITEM_PATTERN.matcher(html);
        while (matcher.find()) {
            String illustId = matcher.group(1);
            String year = matcher.group(2);
            String month = matcher.group(3);
            String day = matcher.group(4);
            String hour = matcher.group(5);
            String minute = matcher.group(6);
            String second = matcher.group(7);
            String title = matcher.group(9);
            String userId = matcher.group(10);
            String userName = matcher.group(11);
            this.log.info(String.format("%s / %s-%s-%s %s:%s:%s / %s / %s / %s",
                                        illustId,
                                        year,
                                        month,
                                        day,
                                        hour,
                                        minute,
                                        second,
                                        title,
                                        userId,
                                        userName));

            this.getIllust(illustId);
        }
    }

    private void findPage(String html) {
        Matcher matcher = NEXT_PATTERN.matcher(html);
        if (matcher.find()) {
            this.page = Integer.valueOf(matcher.group(1));
        } else {
            this.page = -1;
        }
    }

}
