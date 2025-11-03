package app.dto;

import app.dto.external.PackingItemDTO;
import app.entities.Guide;
import app.entities.Trip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {
    private int id;
    private String name;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String locationCoordinates;
    private double price;
    private String category;
    private Set<GuideDTO> guides;
    private List<PackingItemDTO> packingItems;

    public TripDTO(Trip trip) {
        this.id = trip.getId();
        this.name = trip.getName();
        this.startTime = trip.getStartTime();
        this.endTime = trip.getEndTime();
        this.locationCoordinates = trip.getLocationCoordinates();
        this.price = trip.getPrice();
        this.category = trip.getCategory();
        this.guides = trip.getGuides().stream()
                .map(GuideDTO::new)
                .collect(Collectors.toSet());
    }

    public TripDTO(Trip trip, List<PackingItemDTO> packingItems) {
        this(trip);
        this.packingItems = packingItems;
    }

    public Trip toEntity() {
        Trip trip = new Trip();
        trip.setId(this.id);
        trip.setName(this.name);
        trip.setStartTime(this.startTime);
        trip.setEndTime(this.endTime);
        trip.setLocationCoordinates(this.locationCoordinates);
        trip.setPrice(this.price);
        trip.setCategory(this.category);
        return trip;
    }
}
