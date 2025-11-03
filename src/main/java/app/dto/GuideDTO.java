package app.dto;

import app.entities.Guide;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuideDTO {
    private int id;
    private String name;
    private String email;
    private String phone;
    private int yearsOfExperience;

    public GuideDTO(Guide guide) {
        this.id = guide.getId();
        this.name = guide.getName();
        this.email = guide.getEmail();
        this.phone = guide.getPhone();
        this.yearsOfExperience = guide.getYearsOfExperience();
    }

    public Guide toEntity() {
        Guide guide = new Guide();
        guide.setId(this.id);
        guide.setName(this.name);
        guide.setEmail(this.email);
        guide.setPhone(this.phone);
        guide.setYearsOfExperience(this.yearsOfExperience);
        return guide;
    }
}
