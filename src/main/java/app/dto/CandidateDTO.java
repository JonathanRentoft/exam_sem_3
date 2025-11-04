package app.dto;

import app.entities.Candidate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    private Integer id;
    private String name;
    private String phone;
    private String education;
    private Set<SkillDTO> skills = new HashSet<>();

    public CandidateDTO(Candidate candidate) {
        this.id = candidate.getId();
        this.name = candidate.getName();
        this.phone = candidate.getPhone();
        this.education = candidate.getEducation();
        this.skills = candidate.getSkills().stream()
                .map(SkillDTO::new)
                .collect(Collectors.toSet());
    }
}
