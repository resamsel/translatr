import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { MemberRole } from '@dev/translatr-model';
import { AccessTokenService, errorMessage, MemberService, ProjectService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'member',
  type: 'update',
  name: 'Oliver',
  description: 'I\'m going to update the role of a contributor of a random project of mine.',
  weight: 25
};

export class OliverPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly memberService: MemberService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.memberService = injector.get(MemberService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService, { fetch: 'members' }).pipe(
      filter(({ project }) => Boolean(project)),
      map(({ project, accessToken }) => ({
        project,
        accessToken,
        members: project.members.filter(member => member.role !== MemberRole.Owner)
      })),
      filter(({ project, members }) => members.length > 0),
      map(({ project, accessToken, members }) => ({ project, accessToken, members, member: pickRandomly(members) })),
      concatMap(({ project, accessToken, members, member }) =>
        this.memberService.update({
          ...member,
          role: pickRandomly([MemberRole.Translator, MemberRole.Developer].filter(role => role !== member.role))
        }, { params: { access_token: accessToken.key } })
          .pipe(map(member => ({ project, member })))),
      map(
        ({ project, member }) =>
          `role ${member.role} of member ${member.userName} of project ${project.ownerUsername}/${project.name} updated`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new OliverPersona(config, injector)
});
