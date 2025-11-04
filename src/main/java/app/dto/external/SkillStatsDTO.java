package app.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillStatsDTO {
    private String id;
    private String slug;
    private String name;
    private String categoryKey;
    private String description;
    private Integer popularityScore;
    private Integer averageSalary;
    private ZonedDateTime updatedAt;
}
