package com.github.wdxzs1985.pixiv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

public class PixivIllust extends PixivBase implements CommandLineRunner {

    @Value("${illustId}")
    private String illustId;

    @Override
    public void run(String... arg0) throws Exception {
        this.login = this.doLogin();
        this.getIllust(this.illustId);
    }

}
