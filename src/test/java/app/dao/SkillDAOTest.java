package app.dao;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillDAOTest {

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<Skill> query;

    private SkillDAO skillDAO;

    @BeforeEach
    void setUp() {
        skillDAO = new SkillDAO(emf);

        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @Test
    @DisplayName("Create skill should persist and return skill")
    void testCreateSkill() {
        Skill skill = new Skill("Java", "java", SkillCategory.PROG_LANG, "Programming language");

        Skill result = skillDAO.create(skill);

        verify(transaction).begin();
        verify(em).persist(skill);
        verify(transaction).commit();
        verify(em).close();

        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("java", result.getSlug());
    }

    @Test
    @DisplayName("Get by ID should return skill")
    void testGetById() {
        int skillId = 1;
        Skill skill = new Skill("Java", "java", SkillCategory.PROG_LANG, "Programming language");

        when(em.find(Skill.class, skillId)).thenReturn(skill);

        Skill result = skillDAO.getById(skillId);

        verify(em).find(Skill.class, skillId);
        verify(em).close();

        assertNotNull(result);
        assertEquals("Java", result.getName());
    }

    @Test
    @DisplayName("Get by ID should return null when not found")
    void testGetByIdNotFound() {
        int skillId = 999;
        when(em.find(Skill.class, skillId)).thenReturn(null);

        Skill result = skillDAO.getById(skillId);

        verify(em).find(Skill.class, skillId);
        verify(em).close();

        assertNull(result);
    }

    @Test
    @DisplayName("Get all should return list of skills")
    void testGetAll() {
        Skill skill1 = new Skill("Java", "java", SkillCategory.PROG_LANG, "Language");
        Skill skill2 = new Skill("Python", "python", SkillCategory.PROG_LANG, "Language");
        List<Skill> skills = Arrays.asList(skill1, skill2);

        when(em.createQuery("SELECT s FROM Skill s", Skill.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(skills);

        List<Skill> result = skillDAO.getAll();

        verify(em).createQuery("SELECT s FROM Skill s", Skill.class);
        verify(query).getResultList();
        verify(em).close();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getName());
        assertEquals("Python", result.get(1).getName());
    }

    @Test
    @DisplayName("Update skill should merge and return updated skill")
    void testUpdate() {
        Skill skill = new Skill("Java", "java", SkillCategory.PROG_LANG, "Old description");
        skill.setDescription("New description");

        when(em.merge(skill)).thenReturn(skill);

        Skill result = skillDAO.update(skill);

        verify(transaction).begin();
        verify(em).merge(skill);
        verify(transaction).commit();
        verify(em).close();

        assertNotNull(result);
        assertEquals("New description", result.getDescription());
    }


    @DisplayName("Delete skill should remove from database")
    void testDelete() {
    }


    @DisplayName("Delete non-existent skill should not throw exception")
    void testDeleteNotFound() {
    }
}
