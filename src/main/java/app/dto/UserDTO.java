package app.dto;

import app.entities.User;
import app.security.Roles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private int id;
    private String username;
    private String password;
    private Set<Roles> roles;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.roles = user.getRoles();
    }

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
