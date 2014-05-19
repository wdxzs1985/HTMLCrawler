package com.github.wdxzs1985.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.wdxzs1985.domain.PixivUser;

@Service
@Transactional
public class PixivUserService {

    private static final RowMapper<PixivUser> PIXIV_USER_ROW_MAPPER = new RowMapper<PixivUser>() {

        @Override
        public PixivUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            PixivUser user = new PixivUser();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            return user;
        }
    };

    @Autowired
    private final JdbcTemplate jdbc = null;

    public List<PixivUser> getUserList(int page, int pagesize) {
        String sql = "SELECT" + " `id`,"
                + " `name`"
                + " FROM `pixiv_user`"
                + " limit ?, ?;";
        return this.jdbc.query(sql, PIXIV_USER_ROW_MAPPER, page, pagesize);
    }

    public PixivUser getUser(long userId) {
        String sql = "SELECT" + " `id`,"
                + " `name` "
                + " FROM `pixiv_user`"
                + " WHERE `id` = ?;";
        return this.jdbc.queryForObject(sql, PIXIV_USER_ROW_MAPPER, userId);
    }

    public void subscribe(PixivUser user, String email) {
        this.insert(user);
    }

    private void insert(PixivUser user) {
        if (this.isNotExist(user.getId())) {
            String sql = "INSERT INTO " + " `pixiv_user` ("
                    + " `id`,"
                    + " `name`"
                    + ") VALUES ("
                    + " ?,"
                    + " ?"
                    + " );";
            this.jdbc.update(sql, user.getId(), user.getName());
        }
    }

    private boolean isNotExist(long userId) {
        String sql = "SELECT" + " COUNT(`pixiv_user`.`id`)"
                + " FROM `pixiv`.`pixiv_user`"
                + " WHERE `pixiv_user`.`id` = ?;";
        return this.jdbc.queryForObject(sql, Integer.class, userId) == 0;
    }
}
