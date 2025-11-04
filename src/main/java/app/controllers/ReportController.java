package app.controllers;

import app.dao.CandidateDAO;
import app.dto.CandidateDTO;
import app.dto.SkillDTO;
import app.dto.TopCandidateDTO;
import app.entities.Candidate;
import app.services.SkillStatsApiClient;
import io.javalin.http.Context;

import java.util.List;

public class ReportController {
    private final CandidateDAO candidateDAO;
    private final SkillStatsApiClient skillStatsApiClient;

    public ReportController(CandidateDAO candidateDAO, SkillStatsApiClient skillStatsApiClient) {
        this.candidateDAO = candidateDAO;
        this.skillStatsApiClient = skillStatsApiClient;
    }

    public void getTopCandidateByPopularity(Context ctx) {
        List<Candidate> candidates = candidateDAO.getAll();

        TopCandidateDTO topCandidate = null;
        double highestAverage = 0;

        // going through each candidate to find the one with highest average popularity
        for (Candidate candidate : candidates) {
            if (candidate.getSkills().isEmpty()) {
                continue;
            }

            CandidateDTO candidateDTO = new CandidateDTO(candidate);
            candidateDTO.setSkills(skillStatsApiClient.enrichSkills(candidateDTO.getSkills()));

            double sum = 0;
            int count = 0;
            for (SkillDTO skill : candidateDTO.getSkills()) {
                if (skill.getPopularityScore() != null) {
                    sum += skill.getPopularityScore();
                    count++;
                }
            }

            if (count > 0) {
                double average = sum / count;
                if (average > highestAverage) {
                    highestAverage = average;
                    topCandidate = new TopCandidateDTO(candidate.getId(), average);
                }
            }
        }

        ctx.json(topCandidate);
    }
}
