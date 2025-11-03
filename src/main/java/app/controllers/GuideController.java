package app.controllers;

import app.dao.GuideDAO;
import app.dto.GuideDTO;
import app.entities.Guide;
import app.exceptions.ApiException;
import io.javalin.http.Context;

import java.util.List;
import java.util.stream.Collectors;

public class GuideController {
    private final GuideDAO guideDAO;

    public GuideController(GuideDAO guideDAO) {
        this.guideDAO = guideDAO;
    }

    public void getAllGuides(Context ctx) {
        List<Guide> guides = guideDAO.findAll();
        List<GuideDTO> guideDTOs = guides.stream()
                .map(GuideDTO::new)
                .collect(Collectors.toList());
        ctx.json(guideDTOs);
    }

    public void getGuideById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Guide guide = guideDAO.findById(id);

        if (guide == null) {
            throw new ApiException(404, "Guide not found");
        }

        GuideDTO guideDTO = new GuideDTO(guide);
        ctx.json(guideDTO);
    }

    public void createGuide(Context ctx) {
        GuideDTO guideDTO = ctx.bodyAsClass(GuideDTO.class);
        
        if (guideDTO.getName() == null || guideDTO.getName().isEmpty()) {
            throw new ApiException(400, "Guide name is required");
        }

        Guide guide = guideDTO.toEntity();
        Guide created = guideDAO.create(guide);
        GuideDTO responseDTO = new GuideDTO(created);
        ctx.status(201).json(responseDTO);
    }

    public void updateGuide(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Guide existing = guideDAO.findById(id);

        if (existing == null) {
            throw new ApiException(404, "Guide not found");
        }

        GuideDTO guideDTO = ctx.bodyAsClass(GuideDTO.class);
        guideDTO.setId(id);
        
        Guide guide = guideDTO.toEntity();
        guide.setTrips(existing.getTrips()); // Preserve trips
        
        Guide updated = guideDAO.update(guide);
        GuideDTO responseDTO = new GuideDTO(updated);
        ctx.json(responseDTO);
    }

    public void deleteGuide(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Guide guide = guideDAO.findById(id);

        if (guide == null) {
            throw new ApiException(404, "Guide not found");
        }

        guideDAO.delete(id);
        ctx.status(204);
    }
}
