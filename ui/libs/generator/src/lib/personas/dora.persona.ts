import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Activity, Key, Locale, Message, PagedList } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  errorMessage,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProject } from '../project';
import { selectUserAccessToken } from '../user';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'project',
  type: 'read',
  name: 'Dora',
  description: "I'm going to look at a random project.",
  weight: 200
};

export class DoraPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly activityService: ActivityService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.activityService = injector.get(ActivityService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProject(this.projectService).pipe(
      filter(project => Boolean(project)),
      concatMap(project =>
        selectUserAccessToken(this.accessTokenService, this.userService, project.ownerId).pipe(
          concatMap(({ accessToken }) =>
            this.localeService
              .find({
                projectId: project.id,
                limit: 100,
                fetch: 'count',
                access_token: accessToken.key
              })
              .pipe(map((paged: PagedList<Locale>) => ({ accessToken, locales: paged.total })))
          ),
          concatMap(({ accessToken, locales }) =>
            this.keyService
              .find({
                projectId: project.id,
                limit: 100,
                fetch: 'count',
                access_token: accessToken.key
              })
              .pipe(
                map((paged: PagedList<Key>) => ({
                  accessToken,
                  project,
                  locales,
                  keys: paged.total
                }))
              )
          ),
          concatMap(({ accessToken, locales, keys }) =>
            this.messageService
              .find({
                projectId: project.id,
                limit: 100,
                fetch: 'count',
                access_token: accessToken.key
              })
              .pipe(
                map((paged: PagedList<Message>) => ({
                  accessToken,
                  project,
                  locales,
                  keys,
                  messages: paged.total
                }))
              )
          ),
          concatMap(({ accessToken, locales, keys, messages }) =>
            this.activityService
              .find({
                projectId: project.id,
                limit: 100,
                fetch: 'count',
                access_token: accessToken.key
              })
              .pipe(
                map((paged: PagedList<Activity>) => ({
                  accessToken,
                  project,
                  locales,
                  keys,
                  messages,
                  activities: paged.total
                }))
              )
          )
        )
      ),
      map(
        ({ project, locales, keys, messages, activities }) =>
          `project ${project.ownerUsername}/${project.name} with ${locales} languages, ${keys} \
keys, ${messages} translations, and ${activities} activities viewed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new DoraPersona(config, injector)
});
