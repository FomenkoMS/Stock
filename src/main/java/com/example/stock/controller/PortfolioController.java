package com.example.stock.controller;

import com.example.stock.model.PortfolioRequest;
import com.example.stock.model.PortfolioResult;
import com.example.stock.model.SectorAllocation;
import com.example.stock.model.Stock;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private static final String IEX_CLOUD_API_TOKEN = "pk_186de0d362bc4d6f8ca62e06af096ee3";
    private static final String IEX_CLOUD_API_BASE_URL = "https://cloud.iexapis.com/stable";
    private static final Map<String, String> SECTOR_MAP = new HashMap<>();

    static {
        SECTOR_MAP.put("AAPL", "Technology");
        SECTOR_MAP.put("HOG", "Consumer Cyclical");
        SECTOR_MAP.put("MDSO", "Healthcare");
        SECTOR_MAP.put("IDRA", "Healthcare");
        SECTOR_MAP.put("MRSN", "Healthcare");
    }
    private final OkHttpClient client = new OkHttpClient();


    @RequestMapping(value = "/calculate", method = RequestMethod.POST)
    public PortfolioResult calculatePortfolioValue(@RequestBody PortfolioRequest request) throws IOException {
        double value = 0.0;
        Map<String, Double> sectorValues = new HashMap<>();

        for (Stock stock : request.getStocks()) {
            String symbol = stock.getSymbol();
            int volume = stock.getVolume();

            String url = IEX_CLOUD_API_BASE_URL + "/stock/" + symbol + "/quote/latestPrice?token=" + IEX_CLOUD_API_TOKEN;
            Request httpRequest = new Request.Builder().url(url).build();
            Response httpResponse = client.newCall(httpRequest).execute();
            double latestPrice = Double.parseDouble(Objects.requireNonNull(httpResponse.body()).string());

            double assetValue = volume * latestPrice;
            value += assetValue;

            String sector = SECTOR_MAP.get(symbol);
            double sectorValue = sectorValues.getOrDefault(sector, 0.0);
            sectorValue += assetValue;
            sectorValues.put(sector, sectorValue);
        }

        List<SectorAllocation> allocations = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sectorValues.entrySet()) {
            String sector = entry.getKey();
            double sectorValue = entry.getValue();
            double proportion = sectorValue / value;

            allocations.add(new SectorAllocation(sector, sectorValue, proportion));
        }

        PortfolioResult result = new PortfolioResult();
        result.setValue(value);
        result.setAllocationsList(allocations);
        return result;
    }
}
