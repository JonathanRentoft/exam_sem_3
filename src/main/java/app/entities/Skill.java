package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String slug;
    private String description;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    @ManyToMany(mappedBy = "skills")
    private Set<Candidate> candidates = new HashSet<>();

    public Skill(String name, String slug, SkillCategory category, String description) {
        this.name = name;
        this.slug = slug;
        this.category = category;
        this.description = description;
    }
}
