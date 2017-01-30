package services.api;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.ProjectCriteria;
import dto.Project;
import services.api.impl.ProjectApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(ProjectApiServiceImpl.class)
public interface ProjectApiService extends ApiService<Project, UUID, ProjectCriteria> {

}
