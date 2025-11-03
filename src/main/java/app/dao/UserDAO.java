package app.dao;

import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserDAO {
    private final EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public User create(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    public User findByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            try {
                User user = query.getSingleResult();
                user.getRoles().size(); // Initialize lazy collection
                return user;
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    public User findById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, id);
            if (user != null) {
                user.getRoles().size(); // Initialize lazy collection
            }
            return user;
        }
    }
}
