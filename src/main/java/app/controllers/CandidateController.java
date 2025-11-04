package app.controllers;

import app.dao.CandidateDAO;
import app.dto.CandidateDTO;
import app.entities.Candidate;
import app.entities.SkillCategory;
import app.exceptions.ApiException;
import app.services.SkillStatsApiClient;
import io.javalin.http.Context;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateController {
    private final CandidateDAO candidateDAO;
    private final SkillStatsApiClient skillStatsApiClient;

    public CandidateController(CandidateDAO candidateDAO, SkillStatsApiClient skillStatsApiClient) {
        this.candidateDAO = candidateDAO;
        this.skillStatsApiClient = skillStatsApiClient;
    }

    public void getAllCandidates(Context ctx) {
        String categoryParam = ctx.queryParam("category");
        List<Candidate> candidates;

        if (categoryParam != null && !categoryParam.isEmpty()) {
            try {
                SkillCategory category = SkillCategory.valueOf(categoryParam.toUpperCase());
                candidates = candidateDAO.getByCategory(category);
            } catch (IllegalArgumentException e) {
                throw new ApiException(400, "Invalid skill category");
            }
        } else {
            candidates = candidateDAO.getAll();
        }

        List<CandidateDTO> candidateDTOs = candidates.stream()
                .map(CandidateDTO::new)
                .collect(Collectors.toList());
        ctx.json(candidateDTOs);
    }

    public void getCandidateById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Candidate candidate = candidateDAO.getById(id);

        if (candidate == null) {
            throw new ApiException(404, "Candidate not found");
        }

        CandidateDTO candidateDTO = new CandidateDTO(candidate);
        // enriching skills with external api data before returning
        candidateDTO.setSkills(skillStatsApiClient.enrichSkills(candidateDTO.getSkills()));
        ctx.json(candidateDTO);
    }

    public void createCandidate(Context ctx) {
        CandidateDTO candidateDTO = ctx.bodyAsClass(CandidateDTO.class);

        if (candidateDTO.getName() == null || candidateDTO.getName().isEmpty()) {
            throw new ApiException(400, "Candidate name is required");
        }

        Candidate candidate = new Candidate(candidateDTO.getName(), candidateDTO.getPhone(), candidateDTO.getEducation());
        Candidate created = candidateDAO.create(candidate);
        CandidateDTO responseDTO = new CandidateDTO(created);
        ctx.status(201).json(responseDTO);
    }

    public void updateCandidate(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Candidate existing = candidateDAO.getById(id);

        if (existing == null) {
            throw new ApiException(404, "Candidate not found");
        }

        CandidateDTO candidateDTO = ctx.bodyAsClass(CandidateDTO.class);
        existing.setName(candidateDTO.getName());
        existing.setPhone(candidateDTO.getPhone());
        existing.setEducation(candidateDTO.getEducation());

        Candidate updated = candidateDAO.update(existing);
        CandidateDTO responseDTO = new CandidateDTO(updated);
        ctx.json(responseDTO);
    }

    public void deleteCandidate(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Candidate candidate = candidateDAO.getById(id);

        if (candidate == null) {
            throw new ApiException(404, "Candidate not found");
        }

        candidateDAO.delete(id);
        ctx.status(204);
    }

    public void addSkillToCandidate(Context ctx) {
        int candidateId = Integer.parseInt(ctx.pathParam("candidateId"));
        int skillId = Integer.parseInt(ctx.pathParam("skillId"));

        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            throw new ApiException(404, "Candidate not found");
        }

        candidateDAO.addSkillToCandidate(candidateId, skillId);
        Candidate updated = candidateDAO.getById(candidateId);
        CandidateDTO responseDTO = new CandidateDTO(updated);
        ctx.json(responseDTO);
    }
}
