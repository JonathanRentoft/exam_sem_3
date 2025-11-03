package app.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String locationCoordinates;
    private double price;
    private String category;

    @ManyToMany
    @JoinTable(
            name = "trip guide",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn (name = "guide_id")
    )
    private Set<Guide> guides = new HashSet<>();

    public void addGuide(Guide guide) {
        this.guides.add(guide);
        guide.getTrips().add(this);
    }



}
