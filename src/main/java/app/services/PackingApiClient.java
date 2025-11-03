package app.services;

import app.dto.external.PackingItemDTO;
import app.dto.external.PackingListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class PackingApiClient {
    private static final String BASE_URL = "https://packingapi.cphbusinessapps.dk/packinglist/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PackingApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<PackingItemDTO> getPackingList(String category) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + category))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                PackingListDTO packingList = objectMapper.readValue(response.body(), PackingListDTO.class);
                return packingList.getItems();
            } else {
                System.err.println("Failed to fetch packing list: " + response.statusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error fetching packing list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public int getTotalPackingWeight(String category) {
        List<PackingItemDTO> items = getPackingList(category);
        return items.stream()
                .mapToInt(item -> item.getWeightInGrams() * item.getQuantity())
                .sum();
    }
}
