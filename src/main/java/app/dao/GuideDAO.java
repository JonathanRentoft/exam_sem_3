package app.dao;

import app.entities.Guide;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class GuideDAO {
    private final EntityManagerFactory emf;

    public GuideDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Guide create(Guide guide) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(guide);
            em.getTransaction().commit();
            return guide;
        }
    }

    public Guide findById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            Guide guide = em.find(Guide.class, id);
            if (guide != null) {
                guide.getTrips().size(); // Initialize lazy collection
            }
            return guide;
        }
    }

    public List<Guide> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Guide> query = em.createQuery("SELECT g FROM Guide g", Guide.class);
            List<Guide> guides = query.getResultList();
            guides.forEach(guide -> guide.getTrips().size()); // Initialize lazy collections
            return guides;
        }
    }

    public Guide update(Guide guide) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide updated = em.merge(guide);
            em.getTransaction().commit();
            return updated;
        }
    }

    public void delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide guide = em.find(Guide.class, id);
            if (guide != null) {
                em.remove(guide);
            }
            em.getTransaction().commit();
        }
    }
}
