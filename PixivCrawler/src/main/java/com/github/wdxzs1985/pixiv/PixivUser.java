package com.github.wdxzs1985.pixiv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PixivUser extends PixivBase implements CommandLineRunner {

    @Value("${PixivUser.run:false}")
    private boolean isRun;

    @Value("${PixivUser.userId}")
    private String userId;

    @Override
    public void run(String... args) throws Exception {
        if (this.isRun) {
            this.login = this.doLogin();
            this.getPixivUser(this.userId);
        }
    }

}
