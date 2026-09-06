package com.translatr.service;

import com.translatr.dto.StatisticsDto;
import com.translatr.repository.LogEntryRepository;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsService {

    private final UserRepository     userRepo;
    private final ProjectRepository  projectRepo;
    private final LogEntryRepository logRepo;

    @Inject
    public StatisticsService(UserRepository userRepo, ProjectRepository projectRepo, LogEntryRepository logRepo) {
        this.userRepo    = userRepo;
        this.projectRepo = projectRepo;
        this.logRepo     = logRepo;
    }

    public StatisticsDto find() {
        return new StatisticsDto()
                .userCount(userRepo.count())
                .projectCount(projectRepo.count("deleted = false"))
                .activityCount(logRepo.count());
    }
}
