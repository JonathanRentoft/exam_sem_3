package app.services;

import app.dto.SkillDTO;
import app.dto.external.SkillStatsDTO;
import app.dto.external.SkillStatsResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SkillStatsApiClient {
    private static final String BASE_URL = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats";
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public SkillStatsApiClient() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // need these modules to handle ZonedDateTime from the external api response
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // takes a set of skills and enriches them with popularity and salary data from external api
    public Set<SkillDTO> enrichSkills(Set<SkillDTO> skills) {
        if (skills.isEmpty()) {
            return skills;
        }

        String slugs = skills.stream()
                .map(SkillDTO::getSlug)
                .collect(Collectors.joining(","));

        try {
            String url = BASE_URL + "?slugs=" + slugs;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                SkillStatsResponseDTO statsResponse = objectMapper.readValue(response.body(), SkillStatsResponseDTO.class);

                // matching each skill with its stats from the api response
                for (SkillDTO skill : skills) {
                    for (SkillStatsDTO stats : statsResponse.getData()) {
                        if (skill.getSlug().equalsIgnoreCase(stats.getSlug())) {
                            skill.setPopularityScore(stats.getPopularityScore());
                            skill.setAverageSalary(stats.getAverageSalary());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // if external api fails we just return skills without enrichment data
            System.out.println("Failed to fetch skill stats: " + e.getMessage());
        }

        return skills;
    }
}
