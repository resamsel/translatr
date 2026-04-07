package com.translatr.service;

import com.translatr.dto.StatisticsDto;
import com.translatr.repository.LogEntryRepository;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsService {

    @Inject UserRepository     userRepo;
    @Inject ProjectRepository  projectRepo;
    @Inject LogEntryRepository logRepo;

    public StatisticsDto find() {
        StatisticsDto dto = new StatisticsDto();
        dto.userCount     = userRepo.count();
        dto.projectCount  = projectRepo.count("deleted = false");
        dto.activityCount = logRepo.count();
        return dto;
    }
}

