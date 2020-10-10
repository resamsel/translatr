import { Injectable } from '@angular/core';
import {
  AccessTokenCriteria,
  ActivityCriteria,
  Key,
  KeyCriteria,
  Locale,
  LocaleCriteria,
  Member,
  MemberCriteria,
  MemberRole,
  memberRoles,
  MessageCriteria,
  Project,
  User,
  UserRole
} from '@dev/translatr-model';
import { select, Store } from '@ngrx/store';
import { mergeWithError } from '@translatr/utils';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppFacade, defaultParams } from '../../../../+state/app.facade';
import {
  createKey,
  createLocale,
  createMember,
  deleteKey,
  deleteLocale,
  deleteMember,
  loadAccessTokens,
  loadKeys,
  loadLocales,
  loadMembers,
  loadMessages,
  loadProjectActivities,
  loadProjectActivityAggregated,
  unloadProject,
  updateKey,
  updateLocale,
  updateMember
} from './project.actions';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';

const hasRolesAny = (project: Project, user: User, ...roles: MemberRole[]): boolean => {
  if (project === undefined || user === undefined) {
    return false;
  }

  if (user.role === UserRole.Admin) {
    return true;
  }

  if (project.ownerId === user.id) {
    return true;
  }

  if (project.myRole !== undefined) {
    return roles.includes(project.myRole);
  }

  return false;
};

const canAccess = (project: Project, me: User): boolean => hasRolesAny(project, me, ...memberRoles);
const canEdit = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager);
const canDelete = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner);
const canModifyKey = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Developer);
const canModifyLocale = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Translator);
const canModifyMember = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager);

@Injectable()
export class ProjectFacade {
  private _unload$ = new Subject<void>();

  get unload$(): Observable<void> {
    return this._unload$.asObservable();
  }

  project$ = this.appFacade.project$;

  locales$ = this.store.pipe(select(projectQuery.getLocales));
  localesCriteria$ = this.appFacade.criteria$();
  locale$ = this.store.pipe(select(projectQuery.getLocale));
  localeError$ = this.store.pipe(select(projectQuery.getLocaleError));
  localeModified$ = mergeWithError(this.locale$, this.localeError$);

  keys$ = this.store.pipe(select(projectQuery.getKeys));
  keysCriteria$ = this.appFacade.criteria$();
  key$ = this.store.pipe(select(projectQuery.getKey));
  keyError$ = this.store.pipe(select(projectQuery.getKeyError));
  keyModified$ = mergeWithError(this.key$, this.keyError$);

  messages$ = this.store.pipe(select(projectQuery.getMessages));

  members$ = this.store.pipe(select(projectQuery.getMembers));
  membersCriteria$ = this.appFacade.criteria$([...defaultParams, 'roles']);
  member$ = this.store.pipe(select(projectQuery.getMember));
  memberError$ = this.store.pipe(select(projectQuery.getMemberError));
  memberModified$ = mergeWithError(this.member$, this.memberError$);

  activityAggregated$ = this.store.pipe(select(projectQuery.getActivityAggregated));
  activities$ = this.store.pipe(select(projectQuery.getActivities));
  activitiesCriteria$ = this.appFacade.criteria$();

  accessTokens$ = this.store.pipe(select(projectQuery.getAccessTokens));

  canAccess$ = this.appFacade.permission$.pipe(map(([project, me]) => canAccess(project, me)));
  canEdit$ = this.appFacade.permission$.pipe(map(([project, me]) => canEdit(project, me)));
  canDelete$ = this.appFacade.permission$.pipe(map(([project, me]) => canDelete(project, me)));
  canModifyLocale$ = this.appFacade.permission$.pipe(
    map(([project, me]) => canModifyLocale(project, me))
  );
  canModifyKey$ = this.appFacade.permission$.pipe(
    map(([project, me]) => canModifyKey(project, me))
  );
  canModifyMember$ = this.appFacade.permission$.pipe(
    map(([project, me]) => canModifyMember(project, me))
  );

  constructor(
    private readonly store: Store<ProjectPartialState>,
    private readonly appFacade: AppFacade
  ) {}

  loadLocales(projectId: string, criteria?: LocaleCriteria) {
    this.store.dispatch(loadLocales({ payload: { ...criteria, projectId } }));
  }

  loadKeys(projectId: string, criteria?: KeyCriteria) {
    this.store.dispatch(loadKeys({ payload: { ...criteria, projectId } }));
  }

  loadMembers(projectId: string, criteria?: MemberCriteria) {
    this.store.dispatch(loadMembers({ payload: { ...criteria, projectId } }));
  }

  loadMessages(projectId: string, criteria?: MessageCriteria) {
    this.store.dispatch(loadMessages({ payload: { ...criteria, projectId } }));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(loadProjectActivityAggregated({ payload: { id: projectId } }));
  }

  loadActivities(projectId: string, criteria?: ActivityCriteria) {
    this.store.dispatch(loadProjectActivities({ payload: { ...criteria, projectId } }));
  }

  loadAccessTokens(criteria?: AccessTokenCriteria) {
    this.store.dispatch(loadAccessTokens({ payload: criteria }));
  }

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(unloadProject());
    this.appFacade.unloadProject();
  }

  createLocale(locale: Locale) {
    this.store.dispatch(createLocale({ payload: locale }));
  }

  updateLocale(locale: Locale) {
    this.store.dispatch(updateLocale({ payload: locale }));
  }

  deleteLocale(id: string) {
    this.store.dispatch(deleteLocale({ payload: { id } }));
  }

  createKey(key: Key) {
    this.store.dispatch(createKey({ payload: key }));
  }

  updateKey(key: Key) {
    this.store.dispatch(updateKey({ payload: key }));
  }

  deleteKey(id: string) {
    this.store.dispatch(deleteKey({ payload: { id } }));
  }

  createMember(member: Member) {
    this.store.dispatch(createMember({ payload: member }));
  }

  updateMember(member: Member) {
    this.store.dispatch(updateMember({ payload: member }));
  }

  deleteMember(id: number): void {
    this.store.dispatch(deleteMember({ payload: { id } }));
  }
}
