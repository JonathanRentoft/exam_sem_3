package app.dto;

import app.entities.Skill;
import app.entities.SkillCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillDTO {
    private Integer id;
    private String name;
    private String slug;
    private SkillCategory category;
    private String description;
    private Integer popularityScore;
    private Integer averageSalary;

    public SkillDTO(Skill skill) {
        this.id = skill.getId();
        this.name = skill.getName();
        this.slug = skill.getSlug();
        this.category = skill.getCategory();
        this.description = skill.getDescription();
    }
}
