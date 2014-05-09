package com.github.wdxzs1985.pixiv;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;

public abstract class PixivBase {

    private final static Pattern WORKINFO_PATTERN = Pattern.compile("<section class=\"work-info ui-expander-container\"><div class=\"ui-expander-target\"><div class=\"user-reaction\"><section class=\"score\"><dl><dt>閲覧数</dt><dd class=\"view-count\">(\\d+)</dd><dt>評価回数</dt><dd class=\"rated-count\">(\\d+)</dd><dt>総合点</dt><dd class=\"score-count\">(\\d+)</dd></dl><script>pixiv\\.context\\.rated = (true|false);</script><div class=\"rating\"><div class=\"rate\"></div><div class=\"star\"></div><div class=\"status\"></div></div></section></div><ul class=\"meta\"><li>(.*?)</li><li>(.*?)</li><li><ul class=\"tools\">(<li>.*?</li>)*</ul></li>(<li class=\"r-18\">R-18</li>)?(<li><a href=\"/tags.php\\?tag=%E3%82%AA%E3%83%AA%E3%82%B8%E3%83%8A%E3%83%AB\" class=\"original-works _ui-tooltip\" data-tooltip=\"オリジナル作品一覧\">オリジナル</a></li>)?</ul><h1 class=\"title\">(.*?)</h1><p class=\"caption\">(.*?)</p><div class=\"expand\"><span class=\"button\">続きを見る ▾</span></div><div class=\"collapse\"><span class=\"button\">閉じる ▴</span></div></div></section>");
    private final static Pattern ILLUST_IMAGE_PATTERN = Pattern.compile("<div class=\"works_display\"><a href=\"member_illust.php\\?mode=big&illust_id=[\\d]+\" target=\"_blank\"><img src=\"(http://i[\\d]\\.pixiv\\.net/img[0-9]+/img/.*?/\\d+_m.png)\" alt=\"(.*?)\" title=\"(.*?)\" border=\"0\"></a>");
    private final static Pattern ILLUST_TAG_PATTERN = Pattern.compile("<li class=\"tag\"><a href=\"/tags.php\\?tag=.*?\" class=\"portal\">c</a><a href=\"/search.php\\?s_mode=s_tag_full&word=.*?\" class=\"text\">(.*?)</a>");
    private final static Pattern LI_PATTERN = Pattern.compile("<li>(.*?)</li>");

    protected final Log log = LogFactory.getLog(this.getClass());

    protected boolean login = false;

    @Value("${login.pixivId}")
    private String pixivId;

    @Value("${login.pass}")
    private String pass;

    protected boolean doLogin() {
        if (!this.login) {
            String path = "http://www.pixiv.net/login.php";
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair("mode", "login"));
            nvps.add(new BasicNameValuePair("return_to", "/"));
            nvps.add(new BasicNameValuePair("pixiv_id", this.pixivId));
            nvps.add(new BasicNameValuePair("pass", this.pass));
            nvps.add(new BasicNameValuePair("skip", "1"));

            String html = Application.post(path, nvps);
            this.login = StringUtils.isBlank(html);
            if (this.login) {
                this.log.info("login ok");
            }
        }
        return this.login;
    }

    protected void getIllust(String illustId) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=%s",
                                   illustId);
        String html = Application.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = Application.replaceReturn(html);

        this.log.debug(html);

        this.findWorkInfo(html);

        this.findIllustImage(html);
        this.findIllustTags(html);
    }

    protected void findWorkInfo(String html) {
        Matcher matcher = WORKINFO_PATTERN.matcher(html);
        if (matcher.find()) {
            String viewCount = matcher.group(1);
            String ratedCount = matcher.group(2);
            String scoreCount = matcher.group(3);
            String rated = matcher.group(4);
            String date = matcher.group(5);
            String size = matcher.group(6);
            List<String> tools = this.splitListItem(matcher.group(7));
            String R = matcher.group(8);
            String original = matcher.group(9);
            String title = matcher.group(10);
            String caption = matcher.group(11);
            this.log.info(String.format("閲覧数 : %s", viewCount));
            this.log.info(String.format("評価回数: %s", ratedCount));
            this.log.info(String.format("総合点: %s", scoreCount));
            this.log.info(String.format("rated: %s", rated));
            this.log.info(String.format("date: %s", date));
            this.log.info(String.format("size: %s", size));
            for (String tool : tools) {
                this.log.info(String.format("tool: %s", tool));
            }
            this.log.info(String.format("R: %s",
                                        StringUtils.isNotBlank(R) ? "yes"
                                                : "no"));
            this.log.info(String.format("original: %s",
                                        StringUtils.isNotBlank(original) ? "yes"
                                                : "no"));
            this.log.info(String.format("title: %s", title));
            this.log.info(String.format("caption: %s", caption));
        }
    }

    private List<String> splitListItem(String html) {
        List<String> items = new ArrayList<String>();
        Matcher matcher = LI_PATTERN.matcher(html);
        while (matcher.find()) {
            String item = matcher.group(1);
            items.add(item);
        }
        return items;
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
            this.log.info("tag:" + tag);
        }
    }
}
