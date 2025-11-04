# Candidate Matcher Application - Eksamensbesvarelse
### Endpoint tabel

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/login` | POST | 🔓 None | Authenticer bruger |
| `/register` | POST | 🔓 None | Opret ny bruger |
| `/candidates` | GET | 🔒 USER | Hent alle kandidater |
| `/candidates?category=X` | GET | 🔒 USER | Hent filtreret kandidater |
| `/candidates/{id}` | GET | 🔒 USER | Hent kandidat med enriched skills |
| `/candidates` | POST | 🔒 USER | Opret kandidat |
| `/candidates/{id}` | PUT | 🔒 USER | Opdater kandidat |
| `/candidates/{id}` | DELETE | 🔒 ADMIN | Slet kandidat |
| `/candidates/{cId}/skills/{sId}` | PUT | 🔒 USER | Tilføj skill til kandidat |
| `/reports/candidates/top-by-popularity` | GET | 🔒 USER | Hent top kandidat |

## Status

- Jeg er kommet vidt omkring med dette eksamensprojekt, jeg har herunder indsat alle user stories, og lavet en log over mine tanker undervejs som projektet blev lavet. 

Projektet er implementeret med følgende funktionalitet:
- Database konfiguration med Hibernate og JPA
-  Entiteter: Candidate, Skill med mange-til-mange relation
-  CRUD endpoints for kandidater
-  Filtrering af kandidater efter skill kategori
-  Integration med ekstern Skill Stats API
-  Skill enrichment med popularity og salary data
-  Report endpoint for top kandidat efter popularitet
-  JWT authentication og role-based adgangskontrol
-  REST Assured tests
## Designbeslutninger

### US-1: Database konfiguration med Hibernate

**User Story:**
Som systemadministrator vil jeg konfigurere database forbindelsen og entity management med Hibernate, så applikationen kan persistere og hente kandidat- og skill-data på en pålidelig måde.

**Acceptkriterier:**
- Systemet skal inkludere entities for Candidate, Skill, og join table CandidateSkill
- Systemet skal initialisere med sample data via en Populator klasse
- Skills skal have categories som enum

**Beslutninger/tanker:**

1. Jeg har besluttet at bruge en enum (SkillCategory) i stedet for en separat entity klasse, fordi kategorierne er faste værdier der ikke skal ændres dynamisk. Dette gør koden simplere og undgår unødvendige database kald.

2. Jeg har valgt at placere @JoinTable annotationen på Candidate entiteten i stedet for Skill. Dette betyder at Candidate "ejer" relationen, hvilket giver mening fordi vi typisk tilføjer skills til kandidater, ikke omvendt.

3. tilføjet Bidirectional relationship med addSkill metode. Eftersom hibernate nogle gange har problemer med at synkronisere begge sider af en relation, har jeg lavet en addSkill metode i Candidate der opdaterer begge sider. Dette sikrer at relationen altid er konsistent.

4. Jeg har tilføjet et slug felt til Skill for at matche med den eksterne API. Dette kunne også være gjort med en mapping metode, men at have det direkte på entiteten var nemmere. 

### US-2: DAO implementation med CRUD operationer

**User Story:**
Som udvikler vil jeg oprette, læse, opdatere og slette kandidat- og skill-records gennem DAOs, så jeg kan håndtere data konsistent på tværs af applikationen.

**Acceptkriterier:**
- CandidateDAO implementerer CRUD og tillader linking af kandidater og skills
- SkillDAO implementerer basic CRUD
- DTOs bruges til al dataudveksling mellem lag

**Beslutninger/tanker:**

1. Jeg har lavet en generisk IDao interface med basis CRUD metoder, og så ICandidateDao der extender den. Dette er nok en smule overkill for et lille projekt, men er lavet for at vise viser at jeg forstår interfaces og generics.

2. Selvom jeg kunne opdatere en kandidats skills gennem update metoden, har jeg lavet en separat addSkillToCandidate metode. Dette gør intentionen mere klar og giver mulighed for at håndtere fejl specifikt for denne operation.

3. Jeg lader exceptions fra EntityManager boble op til controlleren i stedet for at håndtere dem i DAO'en. Dette holder DAO laget simpelt men betyder at controlleren skal håndtere database fejl.

### US-3: REST endpoints til kandidat management

**User Story:**
Som REST API consumer vil jeg håndtere kandidater gennem HTTP endpoints, så jeg kan udføre standard CRUD operationer og tilknytte skills til kandidater.

**Acceptkriterier:**
- GET /candidates returnerer alle kandidater
- GET /candidates/{id} returnerer kandidat detaljer inklusiv skills
- POST /candidates opretter en kandidat
- PUT /candidates/{id} opdaterer en kandidat
- DELETE /candidates/{id} sletter en kandidat
- PUT /candidates/{candidateId}/skills/{skillId} linker en eksisterende skill til en kandidat

**Beslutninger/tanker:**

1. Jeg validerer kun at name er udfyldt ved oprettelse i controlleren. Mere omfattende validering ville være bedre men tager længerer tid.

2. Jeg bruger statuskode 201 Created ved oprettelse, 204 No Content ved sletning, osv. Jeg følger REST best practices men kræver self at man kender standarderne.

3. For at holde det simpelt og grundet tidspres har jeg ikke implementeret pagination. Dette betyder at hvis der er tusindvis af kandidater, vil de alle blive returneret, hvilket kan være langsomt.
### US-4: Filtrering af kandidater efter skill kategori

**User Story:**
Som recruiter vil jeg se og filtrere kandidater baseret på skill kategori, så jeg kan finde kandidater med specifikke ekspertise områder.

**Acceptkriterier:**
- GET /candidates?category={category} filtrerer kandidater baseret på deres skills' kategori ved hjælp af JPA eller streams

**Beslutninger/tanker:**

1. Jeg konverterer category parameteren til SkillCategory enum og fanger IllegalArgumentException hvis den er invalid. Dette giver en god fejlbesked men er måske ikke den mest elegante løsning.

2. Jeg har valgt at bruge toUpperCase() på category parameteren så både "prog_lang" og "PROG_LANG" virker. Dette gør API'en mere brugervenlig men kan skjule typos.


### US-5: Skill enrichment med ekstern API

**User Story:**
Som recruiter vil jeg se market insights (popularity og salary) for hver kandidats skills, så jeg kan vurdere hvor værdifuldt deres skill set er.

**Acceptkriterier:**
- Ved hentning af kandidat efter ID skal response inkludere enriched skill data fra ekstern Skill Stats API
- Hver skill i response skal inkludere popularityScore og averageSalary
- Hvis kandidaten ikke har skills returneres tom liste
- Hvis en skill er ukendt i ekstern API returneres den uden enrichment data

**Beslutninger/tanker:**

1. Jeg enricher kun skills når man henter en enkelt kandidat, ikke når man henter alle kandidater. Dette reducerer antallet af API kald men betyder derfor at skill data ikke er komplet i listen.

2. Hvis den eksterne API fejler, returnerer jeg bare skills uden enrichment data i stedet for at fejle hele requesten. Dette gør systemet mere robust men skjuler måske problemer.

3. Jeg matcher local skills med API data ved at sammenligne slug i Java kode. Dette kunne også gøres mere effektivt med en Map istedet, men et nested loop er simpelt og fungerer fint for få skills.

### US-6: Top kandidat rapport

**User Story:**
Som analyst vil jeg se kandidaten med højeste gennemsnitlige popularity score, så jeg kan identificere top talent.

**Acceptkriterier:**
- Endpoint: GET /reports/candidates/top-by-popularity returnerer JSON med kandidatens ID og gennemsnitlige popularity score

**Beslutninger/tanker:**

1. Jeg bruger en traditionel for loop til at iterere gennem kandidater i stedet for streams. Dette er naturligvis ikke optimalt men var lettere at debugge i momentet. 

2. Jeg enricher alle kandidaters skills for at beregne gennemsnittet. Dette er ineffektivt hvis der er mange kandidater, men det var en simpel løsning der virker.

3. Jeg springer kandidater over hvis de ikke har skills eller hvis ingen af deres skills har popularity data. Dette undgår division by zero men betyder at kandidaten ikke inkluderes i rapporten.

### US-7: Automated testing

**User Story:**
Som tester vil jeg have automatiserede tests for alle REST endpoints, så applikationens funktionalitet er verificeret og regressioner undgås.

**Acceptkriterier:**
- Hver endpoint har tilsvarende unit/integration tests
- Tests setup mock data og verificerer JSON responses og status codes
- Kandidat-by-ID tests bekræfter at enrichment data er inkluderet

**Beslutninger/tanker:**

1. Jeg bruger H2 i stedet for PostgreSQL til tests. Dette gør tests hurtigere og uafhængige af ekstern database.

2. Jeg har lavet en IntegrationTestBase klasse med fælles setup kode.

3. Jeg bruger REST Assured til at lave rigtige HTTP requests i stedet for at mocke controlleren. Dette giver mere realistiske tests men er langsommere.

4. Jeg opretter tokens i setupTest metoden og gemmer dem som felter. Dette gør tests hurtigere men betyder at hvis token logikken fejler, fejler alle tests.

5. Jeg har kun få tests for fejl cases som 404 og 401. Flere negative tests ville være bedre men tager længere tid at skrive.

### US-8: JWT authentication og role-based access control

**User Story:**
Som secure API consumer vil jeg logge ind og tilgå protected endpoints ved hjælp af en JWT token, så kun autoriserede brugere kan modificere eller se følsomme data.

**Acceptkriterier:**
- POST /login authenticates og returnerer en JWT
- Protected endpoints validerer token og håndhæver roles
- Unauthorized requests returnerer 401 Unauthorized
- Tests verificerer secure access behavior

**Beslutninger/tanker:**

1. Jeg validerer JWT tokens i en beforeMatched hook i stedet for i hver controller metode. Dette centraliserer security logikken.

2. Jeg bruger Role.ANYONE som en marker rolle for endpoints der ikke kræver authentication.

3. Admin rolle har adgang til både user og admin endpoints. Dette er implementeret ved at admins får begge roles ved oprettelse, hvilket er den simpeleste løsning jeg kunne komme på.

### Forudsætninger
- Java 17 eller nyere
- Maven
- PostgreSQL database

### Database setup
Opret en database og opdater `config.properties` med credentials:
```
DB_NAME=candidate_matcher
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## Teknisk stack
- Java 17
- Javalin 6.x
- Hibernate/JPA
- PostgreSQL
- JWT (java-jwt)
- REST Assured til testing
- H2 til test database
- Lombok
- Jackson
