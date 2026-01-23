package com.project.core.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.project.global.config.CloudFunctionProperties;
import com.project.global.exception.core.OperationFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(BatchTriggerClient.class)
@Import(BatchTriggerClientTest.TestConfig.class)
class BatchTriggerClientTest {

    @Autowired
    private BatchTriggerClient client;

    @Autowired
    private MockRestServiceServer server;

    @MockitoBean
    private CloudFunctionProperties properties;

    @TestConfiguration
    static class TestConfig {
        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build(); // ⭐ 핵심
        }
    }

    @Test
    void trigger_success() {
        given(properties.getBaseUrl()).willReturn("http://localhost");

        server.expect(requestTo("http://localhost/run-now"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.trigger("invoiceJob", "202501");

        server.verify();
    }

    @Test
    void schedule_4xx_error() {
        given(properties.getBaseUrl()).willReturn("http://localhost");

        server.expect(requestTo("http://localhost/schedule"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withBadRequest());

        assertThatThrownBy(() ->
                client.schedule("invoiceJob", "0 0 * * *")
        ).isInstanceOf(OperationFailedException.class);
    }

    @Test
    void schedule_5xx_error() {
        given(properties.getBaseUrl()).willReturn("http://localhost");

        server.expect(requestTo("http://localhost/schedule"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        assertThatThrownBy(() ->
                client.schedule("invoiceJob", "0 0 * * *")
        ).isInstanceOf(OperationFailedException.class);
    }

    @Test
    void schedule_unknown_exception() {
        given(properties.getBaseUrl()).willReturn("http://localhost");

        server.expect(requestTo("http://localhost/schedule"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(request -> {
                    throw new RuntimeException("network down");
                });

        assertThatThrownBy(() ->
                client.schedule("invoiceJob", "0 0 * * *")
        ).isInstanceOf(OperationFailedException.class);
    }

}