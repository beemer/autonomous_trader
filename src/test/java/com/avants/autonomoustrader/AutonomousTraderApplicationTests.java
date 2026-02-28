package com.avants.autonomoustrader;

import com.zerodhatech.kiteconnect.KiteConnect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AutonomousTraderApplicationTests {

    @MockBean
    private KiteConnect kiteConnect;

    @Test
    void contextLoads() {
    }
}
