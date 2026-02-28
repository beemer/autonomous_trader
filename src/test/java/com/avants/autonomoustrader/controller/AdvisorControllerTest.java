package com.avants.autonomoustrader.controller;

import com.avants.autonomoustrader.dto.CandidateDto;
import com.avants.autonomoustrader.service.TechnicalScannerService;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TechnicalScannerService technicalScannerService;

    @MockBean
    private KiteConnect kiteConnect;

    @Test
    public void testGetTopCandidatesSerialization() throws Exception {
        CandidateDto candidate = new CandidateDto("RELIANCE", 2500.0, 2400.0, 4.16);
        try {
            when(technicalScannerService.scanWithStrategyParameters()).thenReturn(List.of(candidate));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException e) {
            throw new RuntimeException(e);
        }

        mockMvc.perform(get("/api/v1/advice/top-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("RELIANCE"))
                .andExpect(jsonPath("$[0].currentPrice").value(2500.0))
                .andExpect(jsonPath("$[0].ema200").value(2400.0))
                .andExpect(jsonPath("$[0].distancePct").value(4.16));
    }
}
