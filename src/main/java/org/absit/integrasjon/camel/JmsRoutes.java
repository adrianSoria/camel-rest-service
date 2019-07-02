package org.absit.integrasjon.camel;

import org.absit.integrasjon.repository.DataRepository;
import org.absit.integrasjon.repository.RequestRepository;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JmsRoutes extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsRoutes.class);

    @Value("amq.requestQ")
    private String requestQ;

    private final DataRepository dataRepository;

    private final RequestRepository requestRepository;

    @Autowired
    public JmsRoutes(RequestRepository requestRepository, DataRepository dataRepository) {
        this.requestRepository = requestRepository;
        this.dataRepository = dataRepository;
    }

    @Override
    public void configure() {
        from("activemq:queue:" + requestQ + "?jmsMessageType=Text").routeId("JmsRoutes")
            .log(LoggingLevel.INFO, LOGGER, "Recived JMS message from " + requestQ)
            .bean(requestRepository)
            .log(LoggingLevel.INFO, LOGGER, "Stored JMS request message to DB")
            .bean(dataRepository)
            .log(LoggingLevel.INFO, LOGGER, "Stored DATA to DB");
    }
}
