package app.dao;

import app.entities.Candidate;
import app.entities.SkillCategory;

import java.util.List;

public interface ICandidateDao extends IDao<Candidate> {
    void addSkillToCandidate(int candidateId, int skillId);
    List<Candidate> getByCategory(SkillCategory category);
}
