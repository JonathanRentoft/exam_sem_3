package app.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackingItemDTO {
    private String name;
    private int weightInGrams;
    private int quantity;
    private String description;
    private String category;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<BuyingOptionDTO> buyingOptions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuyingOptionDTO {
        private String shopName;
        private String shopUrl;
        private double price;
    }
}
