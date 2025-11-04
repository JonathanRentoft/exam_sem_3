package app.dao;

import app.entities.Candidate;
import app.entities.Skill;
import app.entities.SkillCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CandidateDAO implements ICandidateDao {
    private final EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Candidate create(Candidate candidate) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(candidate);
            em.getTransaction().commit();
            return candidate;
        }
    }

    @Override
    public Candidate getById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            Candidate candidate = em.find(Candidate.class, id);
            if (candidate != null) {
                // making sure lazy loaded skills are available after closing entity manager
                candidate.getSkills().size();
            }
            return candidate;
        }
    }

    @Override
    public List<Candidate> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Candidate> query = em.createQuery("SELECT c FROM Candidate c", Candidate.class);
            List<Candidate> candidates = query.getResultList();
            // need to initialize skills before returning otherwise we get lazy loading error
            for (Candidate candidate : candidates) {
                candidate.getSkills().size();
            }
            return candidates;
        }
    }

    @Override
    public Candidate update(Candidate candidate) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate updated = em.merge(candidate);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public void delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate candidate = em.find(Candidate.class, id);
            if (candidate != null) {
                em.remove(candidate);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public void addSkillToCandidate(int candidateId, int skillId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate candidate = em.find(Candidate.class, candidateId);
            Skill skill = em.find(Skill.class, skillId);
            if (candidate != null && skill != null) {
                candidate.addSkill(skill);
                em.merge(candidate);
            }
            em.getTransaction().commit();
        }
    }

    @Override
    public List<Candidate> getByCategory(SkillCategory category) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Candidate> query = em.createQuery(
                    "SELECT DISTINCT c FROM Candidate c JOIN c.skills s WHERE s.category = :category",
                    Candidate.class);
            query.setParameter("category", category);
            List<Candidate> candidates = query.getResultList();
            candidates.forEach(c -> c.getSkills().size());
            return candidates;
        }
    }
}
