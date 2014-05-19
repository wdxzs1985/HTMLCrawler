package com.github.wdxzs1985.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.wdxzs1985.domain.PixivIllust;

@Service
@Transactional
public class PixivIllustService {

    private static final RowMapper<PixivIllust> PIXIV_ILLUST_ROW_MAPPER = new RowMapper<PixivIllust>() {

        @Override
        public PixivIllust mapRow(ResultSet rs, int rowNum) throws SQLException {
            PixivIllust illust = new PixivIllust();
            illust.setId(rs.getLong("id"));
            illust.setUserId(rs.getLong("user_id"));
            illust.setTitle(rs.getString("title"));
            illust.setCaption(rs.getString("caption"));
            illust.setMode(rs.getString("mode"));
            return illust;
        }
    };

    @Autowired
    private final JdbcTemplate jdbc = null;

    public List<PixivIllust> getIllustList(int page, int pagesize) {
        String sql = "SELECT" + " `pixiv_illust`.`id`,"
                + " `pixiv_illust`.`title`"
                + " FROM `pixiv`.`pixiv_illust`"
                + " limit ?, ?;";
        return this.jdbc.query(sql, PIXIV_ILLUST_ROW_MAPPER, page, pagesize);
    }

    public List<PixivIllust> getUserIllustList(long userId, int page, int pagesize) {
        String sql = "SELECT" + "`pixiv_illust`.`id`,"
                + " `pixiv_illust`.`title`"
                + " FROM `pixiv`.`pixiv_illust`"
                + " WHERE `pixiv_illust`.`user_id` = ?"
                + " limit ?, ?;";
        return this.jdbc.query(sql,
                               PIXIV_ILLUST_ROW_MAPPER,
                               userId,
                               page,
                               pagesize);
    }

    public PixivIllust getIllust(long illustId) {
        String sql = "SELECT" + "`pixiv_illust`.`id`,"
                + " `pixiv_illust`.`title`"
                + " FROM `pixiv`.`pixiv_illust`"
                + " WHERE `pixiv_illust`.`id` = ?;";
        return this.jdbc.queryForObject(sql, PIXIV_ILLUST_ROW_MAPPER, illustId);
    }

    public void insert(PixivIllust pixivIllust) {
        if (this.isNotExist(pixivIllust.getId())) {
            String sql = "INSERT INTO " + "`pixiv`.`pixiv_illust` ("
                    + " `id`,"
                    + " `user_id`,"
                    + " `title`,"
                    + " `caption`,"
                    + " `mode`"
                    + ") VALUES  ("
                    + " ?,"
                    + " ?,"
                    + " ?,"
                    + " ?,"
                    + " ?"
                    + " );";
            this.jdbc.update(sql,
                             pixivIllust.getId(),
                             pixivIllust.getUserId(),
                             pixivIllust.getTitle(),
                             pixivIllust.getCaption(),
                             pixivIllust.getMode());
        }
    }

    private boolean isNotExist(long illustId) {
        String sql = "SELECT" + " COUNT(`pixiv_illust`.`id`)"
                + " FROM `pixiv`.`pixiv_illust`"
                + " WHERE `pixiv_illust`.`id` = ?;";
        return this.jdbc.queryForObject(sql, Integer.class, illustId) == 0;
    }

    public int bulkInsert(List<PixivIllust> illustList) {
        int count = 0;
        for (PixivIllust pixivIllust : illustList) {
            this.insert(pixivIllust);
            count++;
        }
        return count;
    }
}
