package com.github.wdxzs1985.batch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.wdxzs1985.service.PixivHttpService;

public class PixivTagBatch implements Runnable {

    private final static Pattern IMAGEITEM_PATTERN = Pattern.compile("<li class=\"image-item\"><a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\" class=\"work\"><img src=\"(.*?)\" class=\"_thumbnail\"><h1 class=\"title\" title=\"(.*?)\">.*?</h1></a><a href=\"/member_illust\\.php\\?id=(\\d+)\" class=\"user ui-profile-popup\" title=\".*?\" data-user_id=\"(\\d+)\" data-user_name=\"(.*?)\">.*?</a></li>");
    private final static Pattern NEXT_PATTERN = Pattern.compile("<a href=\"\\?word=[%A-F0-9]+&s_mode=s_tag_full&p=(\\d+)\" rel=\"next\" class=\"_button\" title=\"次へ\">");

    @Value("${PixivTag.word}")
    private String word;

    @Autowired
    private final PixivHttpService service = null;

    private int page = 1;

    @Override
    public void run() {
        if (this.service.doLogin()) {
            this.page = 1;

            if (this.page > 0) {
                this.getPixivTag(this.word, this.page);
            }
        }
    }

    private void getPixivTag(String word, int page) {
        String url = String.format("http://www.pixiv.net/search.php?s_mode=s_tag_full&word=%s&p=%d",
                                   word,
                                   page);
        String html = this.service.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.service.replaceReturn(html);
        this.find(html);
        this.findPage(html);
    }

    private void find(String html) {
        Matcher matcher = IMAGEITEM_PATTERN.matcher(html);
        while (matcher.find()) {
            String illustId = matcher.group(1);
            // this.service.getIllust(illustId);
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
