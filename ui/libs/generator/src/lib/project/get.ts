import { Injector } from '@angular/core';
import { AccessToken, PagedList, Project, User } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { ProjectService } from '@dev/translatr-sdk';
import { map } from 'rxjs/operators';
import { pickRandomly } from '@translatr/utils';

export const getRandomProject = (
  injector: Injector,
  user: User,
  accessToken: AccessToken
): Observable<Project> => {
  const projectService = injector.get(ProjectService);
  return projectService
    .find({
      owner: user.username,
      access_token: accessToken.key
    })
    .pipe(map((pagedList: PagedList<Project>) => pickRandomly(pagedList.list)));
};

