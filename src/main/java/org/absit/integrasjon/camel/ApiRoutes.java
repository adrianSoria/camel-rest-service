package org.absit.integrasjon.camel;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;

import org.absit.integrasjon.dto.api.RequestDto;
import org.absit.integrasjon.dto.api.ResponseDto;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiRoutes extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRoutes.class);

    @Value("amq.requestQ")
    private String requestQ;

    @Override
    public void configure() {

        restConfiguration().component("servlet").port("8080")
            .bindingMode(RestBindingMode.json).skipBindingOnErrorCode(false);

        onException(Exception.class)
            .log(LoggingLevel.ERROR, LOGGER, "Unexpected error: ${exception.stacktrace}")
            .maximumRedeliveries(0)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
            .setBody(constant(null));

        rest("/api/v1/request/")
            .post().type(RequestDto.class)
            .route()
            .setExchangePattern(ExchangePattern.InOnly)
            .log(LoggingLevel.INFO, LOGGER, "Recived a request message")
            .process(exchange -> exchange.getIn().setHeader("resource", UUID.randomUUID().toString()))
            .log(LoggingLevel.INFO, LOGGER, "Created resource")
            .to("activemq:queue:" + requestQ + "?jmsMessageType=Text")
            .setHeader(HttpHeaders.LOCATION, simple("/api/v1/request/${header.resource}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
            .setBody(ResponseDto::new);

        rest("/api/v1/request/{requestId}")
            .get()
            .route()
            .log(LoggingLevel.INFO, LOGGER, "Recived a request message")
            .process(exchange -> exchange.getIn().setBody("data"));
    }
}
