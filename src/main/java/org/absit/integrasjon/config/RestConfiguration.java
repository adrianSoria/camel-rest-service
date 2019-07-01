package org.absit.integrasjon.config;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfiguration {
    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        //noinspection unchecked
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/*");
        servlet.setName("CamelServlet");
        servlet.setAsyncSupported(true);
        return servlet;
    }
}
