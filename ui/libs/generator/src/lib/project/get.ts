import { AccessToken, PagedList, Project, User } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export const getRandomProject = (
  projectService: ProjectService,
  user: User,
  accessToken: AccessToken
): Observable<Project> => {
  return projectService
    .find({
      owner: user.username,
      access_token: accessToken.key
    })
    .pipe(map((pagedList: PagedList<Project>) => pickRandomly(pagedList.list)));
};
