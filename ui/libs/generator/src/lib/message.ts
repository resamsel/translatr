import { AccessToken, Key, Locale, Message, PagedList, Project, Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import { chooseAccessToken } from './access-token';
import { messageSuffix } from './constants';
import { selectRandomProjectAccessToken } from './project';

export const updateMessage = (
  messageService: MessageService,
  project: Project,
  locale: Locale,
  key: Key,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Message> =>
  messageService
    .find({
      projectId: project.id,
      localeId: locale.id,
      keyName: key.name,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.MessageRead
      )
    })
    .pipe(
      map(paged => paged.list[0]),
      concatMap(message => {
        if (message === undefined) {
          message = {
            projectId: project.id,
            localeId: locale.id,
            keyId: key.id,
            value: `${key.name} (${locale.displayName})`
          };
        } else {
          message = {
            ...message,
            value: message.value.endsWith(messageSuffix)
              ? message.value.replace(messageSuffix + '$', '')
              : message.value + messageSuffix
          };
        }
        return messageService[message.id === undefined ? 'create' : 'update'](
          {
            ...message,
            value: message.value
          },
          {
            params: {
              access_token: chooseAccessToken(
                accessToken,
                defaultAccessToken,
                Scope.ProjectRead,
                Scope.MessageWrite
              )
            }
          }
        );
      })
    );

export const deleteRandomMessage = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  defaultAccessToken: string
): Observable<Message> => {
  return selectRandomProjectAccessToken(
    accessTokenService,
    userService,
    projectService,
    localeService,
    keyService,
    messageService
  ).pipe(
    concatMap(({ accessToken, project }) =>
      messageService
        .find({
          projectId: project.id,
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.MessageRead
          )
        })
        .pipe(
          map((pagedList: PagedList<Message>) => ({
            accessToken,
            project,
            messages: pagedList.list
          }))
        )
    ),
    filter(({ messages }) => messages.length > 0),
    concatMap(({ accessToken, project, messages }) =>
      messageService.delete(pickRandomly(messages.map(message => message.id)), {
        params: {
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.MessageWrite
          )
        }
      })
    )
  );
};
