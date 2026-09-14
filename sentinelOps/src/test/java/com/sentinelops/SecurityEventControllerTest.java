package com.sentinelops.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelops.dto.SecurityEventRequest;
import com.sentinelops.model.SecurityEvent;
import com.sentinelops.service.SecurityEventService;

@WebMvcTest(SecurityEventController.class)
class SecurityEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityEventService service;

    @Test
    void shouldReceiveSecurityEvent() throws Exception {

        SecurityEvent event = new SecurityEvent();

        event.setId(1L);
        event.setTimestamp(Instant.parse("2026-09-14T12:00:00Z"));
        event.setSourceip("192.168.1.10");
        event.setMethod("GET");
        event.setPath("/login");
        event.setStatuscode(401);
        event.setSource("apache");
        event.setReceivedAt(Instant.now());

        SecurityEventRequest request = new SecurityEventRequest(
                event.getTimestamp(),
                event.getSourceip(),
                event.getMethod(),
                event.getPath(),
                event.getStatuscode(),
                event.getSource()
        );

        when(service.receiveEvent(any(SecurityEventRequest.class)))
                .thenReturn(event);

        mockMvc.perform(
                post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated());
    }
}
