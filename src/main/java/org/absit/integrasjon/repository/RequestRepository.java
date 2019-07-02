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
public class RequestRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRepository.class);

    private final static String STORE_REQUEST_SQL =
        "INSERT INTO REQUEST (REQUEST_ID, REQUEST) "
            + "VALUES (:forespoersel_id, :forespoersel)";

    @Autowired
    public RequestRepository(NamedParameterJdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    public void lagre(@Header("requestId") final String forespoerselId,
        @Body String forespoersel) {
        final MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("forespoersel_id", "123");
        param.addValue("forespoersel", "reqest");
        jdbcTemplate.update(STORE_REQUEST_SQL, param);

        LOGGER.debug("Opprettet på forespoersel id: {}", forespoerselId);
    }
}
