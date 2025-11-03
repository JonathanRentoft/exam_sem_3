package app.controllers;

import app.dao.UserDAO;
import app.dto.TokenDTO;
import app.dto.UserDTO;
import app.entities.User;
import app.exceptions.ApiException;
import app.security.JwtUtil;
import app.security.Roles;
import io.javalin.http.Context;

public class SecurityController {
    private final UserDAO userDAO;
    private final JwtUtil jwtUtil;

    public SecurityController(UserDAO userDAO, JwtUtil jwtUtil) {
        this.userDAO = userDAO;
        this.jwtUtil = jwtUtil;
    }

    public void login(Context ctx) {
        UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

        if (userDTO.getUsername() == null || userDTO.getPassword() == null) {
            throw new ApiException(400, "Username and password are required");
        }

        User user = userDAO.findByUsername(userDTO.getUsername());

        if (user == null || !user.verifyPassword(userDTO.getPassword())) {
            throw new ApiException(401, "Invalid username or password");
        }

        String token = jwtUtil.createToken(user);
        TokenDTO tokenDTO = new TokenDTO(token, user.getUsername());
        ctx.json(tokenDTO);
    }

    public void register(Context ctx) {
        UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

        if (userDTO.getUsername() == null || userDTO.getPassword() == null) {
            throw new ApiException(400, "Username and password are required");
        }

        User existing = userDAO.findByUsername(userDTO.getUsername());
        if (existing != null) {
            throw new ApiException(400, "Username already exists");
        }

        User user = new User(userDTO.getUsername(), userDTO.getPassword());
        user.addRole(Roles.USER);
        User created = userDAO.create(user);

        String token = jwtUtil.createToken(created);
        TokenDTO tokenDTO = new TokenDTO(token, created.getUsername());
        ctx.status(201).json(tokenDTO);
    }
}
