package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class TripDAO {
    private final EntityManagerFactory emf;

    public TripDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Trip create(Trip trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(trip);
            em.getTransaction().commit();
            return trip;
        }
    }

    public Trip findById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            Trip trip = em.find(Trip.class, id);
            if (trip != null) {
                trip.getGuides().size(); // Initialize lazy collection
            }
            return trip;
        }
    }

    public List<Trip> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t", Trip.class);
            List<Trip> trips = query.getResultList();
            trips.forEach(trip -> trip.getGuides().size()); // Initialize lazy collections
            return trips;
        }
    }

    public List<Trip> findByCategory(String category) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Trip> query = em.createQuery(
                    "SELECT t FROM Trip t WHERE t.category = :category", Trip.class);
            query.setParameter("category", category);
            List<Trip> trips = query.getResultList();
            trips.forEach(trip -> trip.getGuides().size()); // Initialize lazy collections
            return trips;
        }
    }

    public Trip update(Trip trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip updated = em.merge(trip);
            em.getTransaction().commit();
            return updated;
        }
    }

    public void delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip trip = em.find(Trip.class, id);
            if (trip != null) {
                em.remove(trip);
            }
            em.getTransaction().commit();
        }
    }

    public void addGuideToTrip(int tripId, int guideId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip trip = em.find(Trip.class, tripId);
            Guide guide = em.find(Guide.class, guideId);
            if (trip != null && guide != null) {
                trip.addGuide(guide);
                em.merge(trip);
            }
            em.getTransaction().commit();
        }
    }
}
