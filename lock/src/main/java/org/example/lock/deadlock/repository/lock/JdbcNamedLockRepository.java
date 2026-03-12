package org.example.lock.deadlock.repository.lock;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcNamedLockRepository implements NamedLockRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNamedLockRepository(@Qualifier("lockDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Integer getLock(String key, int timeoutSeconds) {
        String sql = "SELECT GET_LOCK(?, ?)";
        return jdbcTemplate.queryForObject(sql, Integer.class, key, timeoutSeconds);
    }

    @Override
    public Integer releaseLock(String key) {
        String sql = "SELECT RELEASE_LOCK(?)";
        return jdbcTemplate.queryForObject(sql, Integer.class, key);
    }
}
