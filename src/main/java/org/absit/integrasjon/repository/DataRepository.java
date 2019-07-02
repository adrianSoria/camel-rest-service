package org.absit.integrasjon.repository;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DataRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRepository.class);

    private final static String STORE_REQUEST_SQL =
        "INSERT INTO DATA (DATA_ID, DATA) "
            + "VALUES (:data_id, :data)";

    @Autowired
    public DataRepository(NamedParameterJdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    public void lagre(@Header("requestId") final String forespoerselId,
        @Body String forespoersel) {
        final MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("data_id", "123");
        param.addValue("data", "reqest");
        jdbcTemplate.update(STORE_REQUEST_SQL, param);
    }
}
