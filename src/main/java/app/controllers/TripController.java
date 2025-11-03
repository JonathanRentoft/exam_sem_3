package app.controllers;

import app.dao.TripDAO;
import app.dto.TripDTO;
import app.dto.external.PackingItemDTO;
import app.entities.Trip;
import app.exceptions.ApiException;
import app.services.PackingApiClient;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripController {
    private final TripDAO tripDAO;
    private final PackingApiClient packingApiClient;

    public TripController(TripDAO tripDAO, PackingApiClient packingApiClient) {
        this.tripDAO = tripDAO;
        this.packingApiClient = packingApiClient;
    }

    public void  getAllTrips(Context ctx) {
        String category = ctx.queryParam("category");
        List<Trip> trips;

        if (category != null && !category.isEmpty()) {
            trips = tripDAO.findByCategory(category);
        } else {
            trips = tripDAO.findAll();
        }

        List<TripDTO> tripDTOs = trips.stream()
                .map(TripDTO::new)
                .collect(Collectors.toList());
        ctx.json(tripDTOs);
    }

    public void getTripById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Trip trip = tripDAO.findById(id);

        if (trip == null) {
            throw new ApiException(404, "Trip not found");
        }

        List<PackingItemDTO> packingItems = packingApiClient.getPackingList(trip.getCategory());
        TripDTO tripDTO = new TripDTO(trip, packingItems);
        ctx.json(tripDTO);
    }

    public void createTrip(Context ctx) {
        TripDTO tripDTO = ctx.bodyAsClass(TripDTO.class);
        
        // Validation
        if (tripDTO.getName() == null || tripDTO.getName().isEmpty()) {
            throw new ApiException(400, "Trip name is required");
        }
        if (tripDTO.getCategory() == null || tripDTO.getCategory().isEmpty()) {
            throw new ApiException(400, "Trip category is required");
        }

        Trip trip = tripDTO.toEntity();
        Trip created = tripDAO.create(trip);
        TripDTO responseDTO = new TripDTO(created);
        ctx.status(201).json(responseDTO);
    }

    public void updateTrip(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Trip existing = tripDAO.findById(id);

        if (existing == null) {
            throw new ApiException(404, "Trip not found");
        }

        TripDTO tripDTO = ctx.bodyAsClass(TripDTO.class);
        tripDTO.setId(id);
        
        Trip trip = tripDTO.toEntity();
        trip.setGuides(existing.getGuides()); // Preserve guides
        
        Trip updated = tripDAO.update(trip);
        TripDTO responseDTO = new TripDTO(updated);
        ctx.json(responseDTO);
    }

    public void deleteTrip(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Trip trip = tripDAO.findById(id);

        if (trip == null) {
            throw new ApiException(404, "Trip not found");
        }

        tripDAO.delete(id);
        ctx.status(204);
    }

    public void addGuideToTrip(Context ctx) {
        int tripId = Integer.parseInt(ctx.pathParam("tripId"));
        int guideId = Integer.parseInt(ctx.pathParam("guideId"));

        Trip trip = tripDAO.findById(tripId);
        if (trip == null) {
            throw new ApiException(404, "Trip not found");
        }

        tripDAO.addGuideToTrip(tripId, guideId);
        Trip updated = tripDAO.findById(tripId);
        TripDTO responseDTO = new TripDTO(updated);
        ctx.json(responseDTO);
    }

    public void getGuidesTotalPrice(Context ctx) {
        List<Trip> trips = tripDAO.findAll();
        Map<Integer, Double> guideTotals = new HashMap<>();

        for (Trip trip : trips) {
            for (var guide : trip.getGuides()) {
                guideTotals.merge(guide.getId(), trip.getPrice(), Double::sum);
            }
        }

        List<Map<String, Object>> result = guideTotals.entrySet().stream()
                .map(entry -> Map.of(
                        "guideId", (Object) entry.getKey(),
                        "totalPrice", entry.getValue()
                ))
                .collect(Collectors.toList());

        ctx.json(result);
    }

    public void getTripPackingWeight(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Trip trip = tripDAO.findById(id);

        if (trip == null) {
            throw new ApiException(404, "Trip not found");
        }

        int totalWeight = packingApiClient.getTotalPackingWeight(trip.getCategory());
        ctx.json(Map.of("tripId", id, "totalWeightInGrams", totalWeight));
    }
}
