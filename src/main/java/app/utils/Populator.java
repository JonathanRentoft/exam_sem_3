package app.utils;

import app.entities.Candidate;
import app.entities.Skill;
import app.entities.SkillCategory;
import app.entities.User;
import app.security.Roles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Populator {

    public static void populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // creating test users for authentication
            User admin = new User("admin", "admin123");
            admin.addRole(Roles.ADMIN);
            admin.addRole(Roles.USER);
            em.persist(admin);

            User user = new User("user", "user123");
            user.addRole(Roles.USER);
            em.persist(user);

            // creating skills first because candidates need to reference them
            Skill java = new Skill("Java", "java", SkillCategory.PROG_LANG, "General-purpose, strongly-typed language for backend and Android");
            em.persist(java);

            Skill python = new Skill("Python", "python", SkillCategory.PROG_LANG, "High-level programming language for web and data science");
            em.persist(python);

            Skill springBoot = new Skill("Spring Boot", "spring-boot", SkillCategory.FRAMEWORK, "Java framework for building microservices and REST APIs");
            em.persist(springBoot);

            Skill postgresql = new Skill("PostgreSQL", "postgresql", SkillCategory.DB, "Open-source relational database with strong SQL compliance");
            em.persist(postgresql);

            Skill docker = new Skill("Docker", "docker", SkillCategory.DEVOPS, "Container platform for deployment");
            em.persist(docker);

            Skill react = new Skill("React", "react", SkillCategory.FRONTEND, "JavaScript library for building user interfaces");
            em.persist(react);

            Skill junit = new Skill("JUnit", "junit", SkillCategory.TESTING, "Testing framework for Java");
            em.persist(junit);

            // adding candidates with different skill combinations
            Candidate candidate1 = new Candidate("John Nielsen", "+45 12 34 56 78", "Computer Science BSc");
            candidate1.addSkill(java);
            candidate1.addSkill(springBoot);
            candidate1.addSkill(postgresql);
            em.persist(candidate1);

            Candidate candidate2 = new Candidate("Maria Hansen", "+45 23 45 67 89", "Software Engineering MSc");
            candidate2.addSkill(python);
            candidate2.addSkill(react);
            em.persist(candidate2);

            Candidate candidate3 = new Candidate("Lars Andersen", "+45 34 56 78 90", "Datamatiker");
            candidate3.addSkill(java);
            candidate3.addSkill(postgresql);
            candidate3.addSkill(docker);
            candidate3.addSkill(junit);
            em.persist(candidate3);

            Candidate candidate4 = new Candidate("Sophie Larsen", "+45 45 67 89 01", "Computer Science MSc");
            candidate4.addSkill(java);
            candidate4.addSkill(springBoot);
            candidate4.addSkill(react);
            em.persist(candidate4);

            em.getTransaction().commit();
            System.out.println("Database populated with sample data");
        } catch (Exception e) {
            System.err.println("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
