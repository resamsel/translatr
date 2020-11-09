import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { MemberRole } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  KeyService,
  LocaleService,
  MemberService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { selectRandomUser } from '../user';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'member',
  type: 'create',
  name: 'Isabella',
  description: "I'm going to add a contributor to a random project of mine.",
  weight: 10
};

export class IsabellaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly messageService: MessageService;
  private readonly memberService: MemberService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
    this.memberService = injector.get(MemberService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService
    ).pipe(
      concatMap(({ accessToken, project }) =>
        selectRandomUser(this.userService).pipe(map(user => ({ accessToken, project, user })))
      ),
      concatMap(({ accessToken, project, user }) =>
        this.memberService
          .create(
            {
              projectId: project.id,
              userId: user.id,
              role: pickRandomly([MemberRole.Translator, MemberRole.Developer])
            },
            { params: { access_token: accessToken.key } }
          )
          .pipe(map(member => ({ project, user, member })))
      ),
      map(
        ({ project, member }) =>
          `member ${member.userName} with role ${member.role} of project ${project.ownerUsername}/${project.name} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new IsabellaPersona(config, injector)
});
