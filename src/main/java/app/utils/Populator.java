package app.utils;

import app.entities.Guide;
import app.entities.Trip;
import app.entities.User;
import app.security.Roles;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.ZonedDateTime;

public class Populator {

    public static void populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Create users
            User admin = new User("admin", "admin123");
            admin.addRole(Roles.ADMIN);
            admin.addRole(Roles.USER);
            em.persist(admin);

            User user = new User("user", "user123");
            user.addRole(Roles.USER);
            em.persist(user);

            // Create guides
            Guide guide1 = new Guide();
            guide1.setName("John Smith");
            guide1.setEmail("john.smith@example.com");
            guide1.setPhone("+45 12 34 56 78");
            guide1.setYearsOfExperience(10);
            em.persist(guide1);

            Guide guide2 = new Guide();
            guide2.setName("Maria Garcia");
            guide2.setEmail("maria.garcia@example.com");
            guide2.setPhone("+45 23 45 67 89");
            guide2.setYearsOfExperience(5);
            em.persist(guide2);

            Guide guide3 = new Guide();
            guide3.setName("Lars Nielsen");
            guide3.setEmail("lars.nielsen@example.com");
            guide3.setPhone("+45 34 56 78 90");
            guide3.setYearsOfExperience(8);
            em.persist(guide3);

            // Create trips
            Trip trip1 = new Trip();
            trip1.setName("Beach Paradise in Cancun");
            trip1.setStartTime(ZonedDateTime.parse("2025-06-01T10:00:00Z"));
            trip1.setEndTime(ZonedDateTime.parse("2025-06-08T18:00:00Z"));
            trip1.setLocationCoordinates("21.1619,-86.8515");
            trip1.setPrice(12500.0);
            trip1.setCategory("beach");
            trip1.addGuide(guide1);
            em.persist(trip1);

            Trip trip2 = new Trip();
            trip2.setName("City Break in Barcelona");
            trip2.setStartTime(ZonedDateTime.parse("2025-07-10T09:00:00Z"));
            trip2.setEndTime(ZonedDateTime.parse("2025-07-15T20:00:00Z"));
            trip2.setLocationCoordinates("41.3874,2.1686");
            trip2.setPrice(8500.0);
            trip2.setCategory("city");
            trip2.addGuide(guide2);
            em.persist(trip2);

            Trip trip3 = new Trip();
            trip3.setName("Forest Adventure in Norway");
            trip3.setStartTime(ZonedDateTime.parse("2025-08-05T08:00:00Z"));
            trip3.setEndTime(ZonedDateTime.parse("2025-08-12T19:00:00Z"));
            trip3.setLocationCoordinates("60.4720,8.4689");
            trip3.setPrice(15000.0);
            trip3.setCategory("forest");
            trip3.addGuide(guide1);
            trip3.addGuide(guide3);
            em.persist(trip3);

            Trip trip4 = new Trip();
            trip4.setName("Lake Relaxation in Como");
            trip4.setStartTime(ZonedDateTime.parse("2025-09-01T10:00:00Z"));
            trip4.setEndTime(ZonedDateTime.parse("2025-09-07T18:00:00Z"));
            trip4.setLocationCoordinates("46.0167,9.2500");
            trip4.setPrice(9500.0);
            trip4.setCategory("lake");
            trip4.addGuide(guide2);
            em.persist(trip4);

            Trip trip5 = new Trip();
            trip5.setName("Sea Cruise in the Mediterranean");
            trip5.setStartTime(ZonedDateTime.parse("2025-10-10T11:00:00Z"));
            trip5.setEndTime(ZonedDateTime.parse("2025-10-20T17:00:00Z"));
            trip5.setLocationCoordinates("36.1408,28.2225");
            trip5.setPrice(22000.0);
            trip5.setCategory("sea");
            trip5.addGuide(guide3);
            em.persist(trip5);

            Trip trip6 = new Trip();
            trip6.setName("Snow Safari in Lapland");
            trip6.setStartTime(ZonedDateTime.parse("2025-12-15T08:00:00Z"));
            trip6.setEndTime(ZonedDateTime.parse("2025-12-22T20:00:00Z"));
            trip6.setLocationCoordinates("68.3600,23.7000");
            trip6.setPrice(18000.0);
            trip6.setCategory("snow");
            trip6.addGuide(guide1);
            trip6.addGuide(guide2);
            em.persist(trip6);

            em.getTransaction().commit();
            System.out.println("Database populated with sample data");
        } catch (Exception e) {
            System.err.println("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
