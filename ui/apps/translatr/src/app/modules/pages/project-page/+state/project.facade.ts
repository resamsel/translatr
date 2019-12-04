import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';
import {
  createKey,
  createLocale,
  deleteKey,
  deleteLocale,
  deleteMember,
  loadKeys,
  loadLocales,
  loadMessages,
  loadProject,
  loadProjectActivities,
  loadProjectActivityAggregated,
  projectLoaded,
  saveProject,
  unloadProject,
  updateKey,
  updateLocale
} from './project.actions';
import { combineLatest, Observable, Subject } from 'rxjs';
import {
  ActivityCriteria,
  Key,
  KeyCriteria,
  Locale,
  LocaleCriteria,
  MemberRole,
  memberRoles,
  Project,
  RequestCriteria,
  User,
  UserRole
} from '@dev/translatr-model';
import { distinctUntilChanged, filter, map, takeUntil } from 'rxjs/operators';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { AppFacade } from '../../../../+state/app.facade';
import { Params } from '@angular/router';

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

  return project.members
    .filter((member) => member.userId === user.id)
    .filter((member) => roles.includes(member.role))
    .length > 0;
};

const canAccess = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, ...memberRoles);
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

  project$ = this.store.pipe(
    select(projectQuery.getProject),
    takeUntil(this.unload$)
  );
  permission$ = combineLatest([this.project$, this.appFacade.me$])
    .pipe(takeUntil(this.unload$));
  canAccess$ = this.permission$.pipe(
    filter(([project]) => !!project),
    map(([project, me]) => canAccess(project, me))
  );
  canEdit$ = this.permission$.pipe(
    filter(([project, me]) => !!project),
    map(([project, me]) => canEdit(project, me))
  );
  canDelete$ = this.permission$.pipe(
    map(([project, me]) => canDelete(project, me))
  );

  criteria$ = this.appFacade.queryParams$.pipe(
    map((params: Params) => ['search', 'limit', 'offset']
      .filter(f => params[f] !== undefined && params[f] !== '')
      .reduce((acc, curr) => ({ ...acc, [curr]: params[curr] }), {})
    ),
    distinctUntilChanged((a: RequestCriteria, b: RequestCriteria) =>
      a.search === b.search && a.limit === b.limit && a.offset === b.offset)
  );

  locales$ = this.store.pipe(
    select(projectQuery.getLocales),
    takeUntil(this.unload$)
  );
  localesCriteria$ = this.criteria$;
  localeModified$ = this.store.pipe(
    select(projectQuery.getLocale),
    takeUntil(this.unload$)
  );
  localeModifiedError$ = this.store.pipe(
    select(projectQuery.getLocaleError),
    takeUntil(this.unload$)
  );
  canModifyLocale$ = this.permission$.pipe(
    map(([project, me]) => canModifyLocale(project, me))
  );

  keys$ = this.store.pipe(
    select(projectQuery.getKeys),
    takeUntil(this.unload$)
  );
  keysCriteria$ = this.criteria$;
  keyModified$ = this.store.pipe(
    select(projectQuery.getKey),
    takeUntil(this.unload$)
  );
  keyModifiedError$ = this.store.pipe(
    select(projectQuery.getKeyError),
    takeUntil(this.unload$)
  );
  canModifyKey$ = this.permission$.pipe(
    map(([project, me]) => canModifyKey(project, me))
  );

  messages$ = this.store.pipe(
    select(projectQuery.getMessages),
    takeUntil(this.unload$)
  );

  canModifyMember$ = this.permission$.pipe(
    map(([project, me]) => canModifyMember(project, me))
  );
  memberModified$ = this.store.pipe(
    select(projectQuery.getMember),
    takeUntil(this.unload$)
  );

  activityAggregated$ = this.store.pipe(
    select(projectQuery.getActivityAggregated),
    takeUntil(this.unload$)
  );
  activities$ = this.store.pipe(
    select(projectQuery.getActivities),
    takeUntil(this.unload$)
  );

  constructor(
    private readonly store: Store<ProjectPartialState>,
    private readonly appFacade: AppFacade
  ) {
  }

  loadProject(username: string, projectName: string) {
    this.store.dispatch(loadProject({ payload: { username, projectName } }));
  }

  loadLocales(projectId: string, criteria?: LocaleCriteria) {
    this.store.dispatch(loadLocales({ payload: { ...criteria, projectId } }));
  }

  loadKeys(projectId: string, criteria?: KeyCriteria) {
    this.store.dispatch(loadKeys({ payload: { ...criteria, projectId } }));
  }

  loadMessages(projectId: string, criteria?: MessageCriteria) {
    this.store.dispatch(loadMessages({ payload: { ...criteria, projectId } }));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(loadProjectActivityAggregated({ payload: { id: projectId } }));
  }

  loadActivities(criteria: ActivityCriteria) {
    this.store.dispatch(loadProjectActivities({ payload: criteria }));
  }

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(unloadProject());
  }

  save(project: Project) {
    this.store.dispatch(saveProject({ payload: project }));
  }

  projectLoaded(project: Project) {
    this.store.dispatch(projectLoaded({ payload: project }));
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

  deleteMember(id: number): void {
    this.store.dispatch(deleteMember({ payload: { id } }));
  }
}
