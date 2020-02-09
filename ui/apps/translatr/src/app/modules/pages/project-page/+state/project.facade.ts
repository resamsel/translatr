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
  loadModifiers,
  loadProject,
  loadProjectActivities,
  loadProjectActivityAggregated,
  projectLoaded,
  saveProject,
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
  members: Member[],
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

  if (members === undefined) {
    return false;
  }

  return members
    .filter((member) => member.userId === user.id)
    .filter((member) => roles.includes(member.role))
    .length > 0;
};

const canAccess = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, ...memberRoles);
const canEdit = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, MemberRole.Owner, MemberRole.Manager);
const canDelete = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, MemberRole.Owner);
const canModifyKey = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Developer);
const canModifyLocale = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Translator);
const canModifyMember = (project: Project, members: Member[], me: User): boolean =>
  hasRolesAny(project, members, me, MemberRole.Owner, MemberRole.Manager);

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
  modifiers$ = this.store.pipe(
    select(projectQuery.getModifiers),
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
    this.modifiers$.pipe(filter(x => !!x)),
    this.appFacade.me$
  ])
    .pipe(takeUntil(this.unload$));
  canAccess$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canAccess(project, members.list, me))
  );
  canEdit$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canEdit(project, members.list, me))
  );
  canDelete$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canDelete(project, members.list, me))
  );
  canModifyLocale$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canModifyLocale(project, members.list, me))
  );
  canModifyKey$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canModifyKey(project, members.list, me))
  );
  canModifyMember$ = this.permission$.pipe(
    map(([project, members, me]) =>
      canModifyMember(project, members.list, me))
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

  loadMembers(projectId: string, criteria?: MemberCriteria) {
    this.store.dispatch(loadMembers({ payload: { ...criteria, projectId } }));
  }

  loadModifiers(projectId: string) {
    this.store.dispatch(loadModifiers({
      payload: {
        projectId,
        limit: 1000,
        roles: 'Owner,Manager'
      }
    }));
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
