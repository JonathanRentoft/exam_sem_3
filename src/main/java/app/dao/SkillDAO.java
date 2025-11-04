package app.dao;

import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SkillDAO implements ISkillDao {
    private final EntityManagerFactory emf;

    public SkillDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Skill create(Skill skill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(skill);
            em.getTransaction().commit();
            return skill;
        }
    }

    @Override
    public Skill getById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Skill.class, id);
        }
    }

    @Override
    public List<Skill> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Skill> query = em.createQuery("SELECT s FROM Skill s", Skill.class);
            return query.getResultList();
        }
    }

    @Override
    public Skill update(Skill skill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill updated = em.merge(skill);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public void delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill skill = em.find(Skill.class, id);
            if (skill != null) {
                em.remove(skill);
            }
            em.getTransaction().commit();
        }
    }
}
