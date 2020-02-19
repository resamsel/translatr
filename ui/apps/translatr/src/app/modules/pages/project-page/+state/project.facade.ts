import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';
import {
  createKey,
  createLocale,
  createMember,
  deleteKey,
  deleteLocale,
  deleteMember,
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
import { combineLatest, merge, Observable, Subject } from 'rxjs';
import {
  ActivityCriteria,
  Key,
  KeyCriteria,
  Locale,
  LocaleCriteria,
  Member,
  MemberCriteria,
  MemberRole,
  memberRoles,
  Project,
  User,
  UserRole
} from '@dev/translatr-model';
import { filter, map, takeUntil } from 'rxjs/operators';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { AppFacade, defaultParams } from '../../../../+state/app.facade';

const hasRolesAny = (
  project: Project,
  user: User,
  ...roles: MemberRole[]
): boolean => {
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

  project$ = this.appFacade.project$.pipe(
    takeUntil(this.unload$)
  );

  locales$ = this.store.pipe(
    select(projectQuery.getLocales),
    takeUntil(this.unload$)
  );
  localesCriteria$ = this.appFacade.criteria$();
  localeModified$ = this.store.pipe(
    select(projectQuery.getLocale),
    takeUntil(this.unload$)
  );
  localeModifiedError$ = this.store.pipe(
    select(projectQuery.getLocaleError),
    takeUntil(this.unload$)
  );

  keys$ = this.store.pipe(
    select(projectQuery.getKeys),
    takeUntil(this.unload$)
  );
  keysCriteria$ = this.appFacade.criteria$();
  keyModified$ = this.store.pipe(
    select(projectQuery.getKey),
    takeUntil(this.unload$)
  );
  keyModifiedError$ = this.store.pipe(
    select(projectQuery.getKeyError),
    takeUntil(this.unload$)
  );

  messages$ = this.store.pipe(
    select(projectQuery.getMessages),
    takeUntil(this.unload$)
  );

  members$ = this.store.pipe(
    select(projectQuery.getMembers),
    takeUntil(this.unload$)
  );
  membersCriteria$ = this.appFacade.criteria$([...defaultParams, 'roles']);
  memberModified$ = merge(
    this.store.pipe(select(projectQuery.getMember)),
    this.store.pipe(select(projectQuery.getMemberError))
  )
    .pipe(takeUntil(this.unload$));

  activityAggregated$ = this.store.pipe(
    select(projectQuery.getActivityAggregated),
    takeUntil(this.unload$)
  );
  activities$ = this.store.pipe(
    select(projectQuery.getActivities),
    takeUntil(this.unload$)
  );
  activitiesCriteria$ = this.appFacade.criteria$();

  permission$ = combineLatest([
    this.project$.pipe(filter(x => !!x)),
    this.appFacade.me$
  ])
    .pipe(takeUntil(this.unload$));
  canAccess$ = this.permission$.pipe(
    map(([project, me]) => canAccess(project, me))
  );
  canEdit$ = this.permission$.pipe(
    map(([project, me]) => canEdit(project, me))
  );
  canDelete$ = this.permission$.pipe(
    map(([project, me]) => canDelete(project, me))
  );
  canModifyLocale$ = this.permission$.pipe(
    map(([project, me]) => canModifyLocale(project, me))
  );
  canModifyKey$ = this.permission$.pipe(
    map(([project, me]) => canModifyKey(project, me))
  );
  canModifyMember$ = this.permission$.pipe(
    map(([project, me]) => canModifyMember(project, me))
  );

  constructor(
    private readonly store: Store<ProjectPartialState>,
    private readonly appFacade: AppFacade
  ) {
  }

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

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(unloadProject());
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
