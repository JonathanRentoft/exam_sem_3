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
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String phone;
    private String education;

    @ManyToMany
    @JoinTable(
            name = "candidate_skill",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    public Candidate(String name, String phone, String education) {
        this.name = name;
        this.phone = phone;
        this.education = education;
    }

    // adds skill to candidate and updates both sides of the relationship
    public void addSkill(Skill skill) {
        this.skills.add(skill);
        skill.getCandidates().add(this);
    }
}
