package org.absit.integrasjon;

import static org.hamcrest.core.StringContains.containsString;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;

import org.absit.integrasjon.camel.ApiRoutes;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
public class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRoutes.class);
    private static final String TOKEN = "testToken";

    @LocalServerPort
    protected String randomServerPort;
    private String serviceUrl;

    @Before
    public void createTestSetup() {
        serviceUrl = "http://localhost:" + randomServerPort + "/api/v1/request/";
    }

    @Test
    public void post_request_shouldCreateResource() {
        given()
            .header(HttpHeaders.ACCEPT, TOKEN)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(getResposeFromFile("apiRequests/Request.json"))
        .when()
            .post(serviceUrl)
        .then()
            .statusCode(201);
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
}
