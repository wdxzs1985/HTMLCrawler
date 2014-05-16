package com.github.wdxzs1985.pixiv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PixivIllust extends PixivBase implements CommandLineRunner {

    @Value("${PixivIllust.run:false}")
    private boolean isRun;

    @Value("${PixivIllust.illustId}")
    private String illustId;

    @Override
    public void run(String... args) throws Exception {
        if (this.isRun) {
            this.login = this.doLogin();
            this.getIllust(this.illustId);
        }
    }

}
