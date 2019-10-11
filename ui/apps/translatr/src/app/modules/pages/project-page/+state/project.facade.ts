import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';
import {
  LoadKeys,
  LoadLocales,
  LoadMessages,
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  SaveProject,
  UnloadProject
} from './project.actions';
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { ActivityCriteria, KeyCriteria, LocaleCriteria, MemberRole, Project, User } from '@dev/translatr-model';
import { map, takeUntil } from 'rxjs/operators';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { AppFacade } from 'apps/translatr/src/app/+state/app.facade';

const hasRolesAny = (project: Project, user: User, ...roles: MemberRole[]): boolean => {
  if (project === undefined || user === undefined) {
    return false;
  }

  if (project.ownerId === user.id) {
    return true;
  }

  return project.members
    .filter((member) => member.userId === user.id)
    .filter((member) => roles.includes(member.role))
    .length > 0;
};

const canEdit = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager);
const canDelete = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner);
const canCreateKey = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Developer);
const canCreateLocale = (project: Project, me: User): boolean =>
  hasRolesAny(project, me, MemberRole.Owner, MemberRole.Manager, MemberRole.Translator);
const canCreateMember = (project: Project, me: User): boolean =>
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
  permission$ = combineLatest([this.project$, this.appFacade.me$]);
  canEdit$ = this.permission$.pipe(
    map(([project, me]) => canEdit(project, me))
  );
  canDelete$ = this.permission$.pipe(
    map(([project, me]) => canDelete(project, me))
  );

  locales$ = this.store.pipe(
    select(projectQuery.getLocales),
    takeUntil(this.unload$)
  );
  localesCriteria$ = new BehaviorSubject<LocaleCriteria | undefined>(undefined);
  canCreateLocale$ = this.permission$.pipe(
    map(([project, me]) => canCreateLocale(project, me))
  );

  keys$ = this.store.pipe(
    select(projectQuery.getKeys),
    takeUntil(this.unload$)
  );
  keysCriteria$ = new BehaviorSubject<KeyCriteria | undefined>(undefined);
  canCreateKey$ = this.permission$.pipe(
    map(([project, me]) => canCreateKey(project, me))
  );

  messages$ = this.store.pipe(
    select(projectQuery.getMessages),
    takeUntil(this.unload$)
  );

  canCreateMember$ = this.permission$.pipe(
    map(([project, me]) => canCreateMember(project, me))
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
    this.store.dispatch(new LoadProject({ username, projectName }));
  }

  loadLocales(projectId: string, criteria?: LocaleCriteria) {
    this.localesCriteria$.next(criteria);
    this.store.dispatch(new LoadLocales({ ...criteria, projectId }));
  }

  loadKeys(projectId: string, criteria?: KeyCriteria) {
    this.keysCriteria$.next(criteria);
    this.store.dispatch(new LoadKeys({ ...criteria, projectId }));
  }

  loadMessages(projectId: string, criteria?: MessageCriteria) {
    this.store.dispatch(new LoadMessages({ ...criteria, projectId }));
  }

  loadActivityAggregated(projectId: string) {
    this.store.dispatch(new LoadProjectActivityAggregated({ id: projectId }));
  }

  loadActivities(criteria: ActivityCriteria) {
    this.store.dispatch(new LoadProjectActivities(criteria));
  }

  unloadProject() {
    this._unload$.next();
    this.store.dispatch(new UnloadProject());
  }

  save(project: Project) {
    this.store.dispatch(
      new SaveProject({
        id: project.id,
        name: project.name
      })
    );
  }
}
