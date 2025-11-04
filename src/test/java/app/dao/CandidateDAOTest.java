package app.dao;

import app.entities.Candidate;
import app.entities.Skill;
import app.entities.SkillCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateDAOTest {

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<Candidate> query;

    private CandidateDAO candidateDAO;

    @BeforeEach
    void setUp() {
        candidateDAO = new CandidateDAO(emf);

        // setup default mock behavior
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @Test
    @DisplayName("Create candidate should persist and return candidate")
    void testCreateCandidate() {
        Candidate candidate = new Candidate("John Doe", "+45 12345678", "Computer Science");

        Candidate result = candidateDAO.create(candidate);


        verify(transaction).begin();
        verify(em).persist(candidate);
        verify(transaction).commit();
        verify(em).close();

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("Get by ID should return candidate with initialized skills")
    void testGetById() {
        int candidateId = 1;
        Candidate candidate = new Candidate("John Doe", "+45 12345678", "Computer Science");
        candidate.getSkills().add(new Skill("Java", "java", SkillCategory.PROG_LANG, "Programming language"));

        when(em.find(Candidate.class, candidateId)).thenReturn(candidate);

        Candidate result = candidateDAO.getById(candidateId);

        verify(em).find(Candidate.class, candidateId);
        verify(em).close();

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertFalse(result.getSkills().isEmpty());
    }

    @Test
    @DisplayName("Get by ID should return null when candidate not found")
    void testGetByIdNotFound() {

    }

    @Test
    @DisplayName("Get all should return list of candidates with initialized skills")
    void testGetAll() {
    }

    @Test
    @DisplayName("Update candidate should merge and return updated candidate")
    void testUpdate() {
        Candidate candidate = new Candidate("John Doe", "+45 12345678", "Computer Science");
        candidate.setName("John Updated");

        when(em.merge(candidate)).thenReturn(candidate);

        Candidate result = candidateDAO.update(candidate);

        verify(transaction).begin();
        verify(em).merge(candidate);
        verify(transaction).commit();
        verify(em).close();

        assertNotNull(result);
        assertEquals("John Updated", result.getName());
    }

    @Test
    @DisplayName("Delete candidate should remove from database")
    void testDelete() {
        int candidateId = 1;
        Candidate candidate = new Candidate("John Doe", "+45 12345678", "Computer Science");

        when(em.find(Candidate.class, candidateId)).thenReturn(candidate);

        candidateDAO.delete(candidateId);

        verify(transaction).begin();
        verify(em).find(Candidate.class, candidateId);
        verify(em).remove(candidate);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    @DisplayName("Delete non-existent candidate should not throw exception")
    void testDeleteNotFound() {

    }

    @Test
    @DisplayName("Add skill to candidate should update both entities")
    void testAddSkillToCandidate() {

    }

    @Test
    @DisplayName("Add skill to non-existent candidate should not throw exception")
    void testAddSkillToCandidateNotFound() {

    }

    @Test
    @DisplayName("Get by category should return candidates with skills in that category")
    void testGetByCategory() {

    }

    @Test
    @DisplayName("Get by category should return empty list when no matches")
    void testGetByCategoryNoMatches() {

    }
}
