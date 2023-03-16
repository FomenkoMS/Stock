package com.example.stock;

import com.example.stock.model.PortfolioRequest;
import com.example.stock.model.PortfolioResult;
import com.example.stock.model.Stock;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StockApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Test
    void contextLoads() {
    }

    @Test
    void calculatePortfolioValue() throws Exception {

        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("AAPL", 200));
        stocks.add(new Stock("HOG", 10));
        stocks.add(new Stock("MDSO", 200));
        stocks.add(new Stock("MRSN", 200));
        PortfolioRequest request = new PortfolioRequest(stocks);

        String w = new ObjectMapper().writeValueAsString(request);

        MvcResult resultActions = mockMvc.perform(post("http://localhost:8000/api/portfolio/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(w))
                .andExpect(status().isOk())
                .andReturn();

        PortfolioResult portfolioResult = new ObjectMapper().readValue(resultActions.getResponse().getContentAsString(), PortfolioResult.class);
        assertEquals(portfolioResult.getAllocationsList().get(0).getSector(), "Technology");
    }

}
