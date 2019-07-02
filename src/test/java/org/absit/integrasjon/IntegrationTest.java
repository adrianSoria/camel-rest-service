package org.absit.integrasjon;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.core.HttpHeaders;

import org.absit.integrasjon.camel.ApiRoutes;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Queue;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
public class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRoutes.class);
    private static final String TOKEN = "testToken";

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    protected String randomServerPort;
    private String serviceUrl;

    @Before
    public void createTestSetup() throws Exception {
        cleanQueues(getEmbeddedBroker());
        serviceUrl = "http://localhost:" + randomServerPort + "/api/v1/request/";
    }

    @Test
    public void post_request_shouldCreateResourceAndRollbackQueueAndDbOnException() throws InterruptedException {
        given()
            .header(HttpHeaders.ACCEPT, TOKEN)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(getResposeFromFile("apiRequests/Request.json"))
        .when()
            .post(serviceUrl)
        .then()
            .statusCode(201);

        Thread.sleep(5000); // wait for commit and rollback.
        await().until(anyRequestRows(), is(0));
        await().until(anyDataRows(), is(0));

    }

    @Test
    public void get_request_shouldRetreive() {
        given()
            .header(HttpHeaders.ACCEPT, TOKEN)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .when()
            .get(serviceUrl + "9565644")
        .then()
            .body(containsString("data"))
            .statusCode(200);
    }

    public static String getResposeFromFile(final String fileName) {
        File responseMessage;
        try {
            responseMessage = new ClassPathResource(fileName).getFile();
            return FileUtils.readFileToString(responseMessage, "utf-8");

        } catch (IOException e) {
            LOGGER.warn("Error when reading file " + e.getMessage());
        }
        return null;
    }

    private Broker getEmbeddedBroker() throws Exception {
        Map<String,BrokerService> brokers = BrokerRegistry.getInstance().getBrokers();
        BrokerService brokerService = brokers.get("embedded");
        return brokerService.getBroker();
    }

    public void cleanQueues(Broker broker) throws Exception {
        Map<ActiveMQDestination, Destination> destinationMap
            = broker.getDestinationMap();
        for (Destination destination : destinationMap.values()) {
            ActiveMQDestination activeMQDestination
                = destination.getActiveMQDestination();
            if (activeMQDestination.isQueue()) {
                cleanQueue((Queue) destination);
            }
        }
    }

    private void cleanQueue(Queue queue) throws Exception {
        queue.purge();
    }

    private Callable<Integer> anyRequestRows() {
        return () -> JdbcTestUtils.countRowsInTable(jdbcTemplate,
            "REQUEST");
    }

    private Callable<Integer> anyDataRows() {
        return () -> JdbcTestUtils.countRowsInTable(jdbcTemplate,
            "DATA");
    }

}
