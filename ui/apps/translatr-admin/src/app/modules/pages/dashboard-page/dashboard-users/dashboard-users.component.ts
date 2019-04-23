import { Component, OnDestroy } from '@angular/core';
import { AppFacade } from "../../../../+state/app.facade";
import { RequestCriteria, User } from "@dev/translatr-model";
import { debounceTime, distinctUntilChanged, map, mapTo, shareReplay, startWith, take } from "rxjs/operators";
import { UserDeleted, UserDeleteError, UsersDeleted, UsersDeleteError } from "../../../../+state/app.actions";
import {
  UserEditDialogComponent,
  UserEditDialogConfig
} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.component";
import { MatDialog, MatSnackBar } from "@angular/material";
import { filter } from "rxjs/internal/operators/filter";
import { UserRole } from "@dev/translatr-model/src/lib/model/user-role";
import { merge, Observable, Subject } from "rxjs";
import { scan } from "rxjs/internal/operators/scan";
import {
  hasCreateUserPermission,
  hasDeleteAllUsersPermission,
  hasDeleteUserPermission,
  hasEditUserPermission,
  isAdmin
} from "@dev/translatr-sdk/src/lib/shared/permissions";
import { Entity } from "@dev/translatr-components";

export const mapToAllowedRoles = () => map((me?: User): UserRole[] =>
  [UserRole.User, ...isAdmin(me) ? [UserRole.Admin] : []]);

@Component({
  selector: 'dev-dashboard-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.scss']
})
export class DashboardUsersComponent implements OnDestroy {

  readonly displayedColumns = ['name', 'username', 'email', 'when_created', 'role', 'actions'];

  me$ = this.facade.me$;
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  reload$ = new Subject<void>();
  commands$ = merge(
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({search}))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({limit: `${limit}`}))
    ),
    this.reload$.asObservable().pipe(mapTo({}))
  )
    .pipe(
      startWith({limit: '20', search: '', order: 'name asc'}),
      scan((acc: RequestCriteria, value: RequestCriteria) => ({...acc, ...value})),
      shareReplay(1)
    );
  users$ = this.facade.users$;
  allowCreate$ = this.me$.pipe(hasCreateUserPermission());

  selected: Entity[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.commands$.subscribe((criteria: RequestCriteria) => this.facade.loadUsers(criteria));
    this.facade.userDeleted$
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          snackBar.open(`User ${action.payload.username} has been deleted`, 'Dismiss', {duration: 3000});
          this.reload$.next();
        } else {
          snackBar.open(`User could not be deleted: ${action.payload.error.error}`, 'Dismiss', {duration: 8000});
        }
      });
    this.facade.usersDeleted$
      .subscribe((action: UsersDeleted | UsersDeleteError) => {
        if (action instanceof UsersDeleted) {
          snackBar.open(`${action.payload.length} users have been deleted`, 'Dismiss', {duration: 3000});
        } else {
          snackBar.open(`Users could not be deleted: ${action.payload.error.error}`, 'Dismiss', {duration: 8000});
        }
        this.reload$.next();
      });
  }

  onSelected(entities: Entity[]) {
    this.selected = entities;
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2));
  }

  allowEdit$(user: User): Observable<boolean> {
    return this.me$.pipe(hasEditUserPermission(user));
  }

  allowDelete$(user: User): Observable<boolean> {
    return this.me$.pipe(hasDeleteUserPermission(user));
  }

  allowDeleteAll$(users: User[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllUsersPermission(users));
  }

  onCreate() {
    this.me$.pipe(take(1), mapToAllowedRoles())
      .subscribe((allowedRoles: UserRole[]) => {
        this.dialog
          .open(UserEditDialogComponent, {
            data: {
              type: 'create',
              allowedRoles,
              onSubmit: (user: User) => this.facade.createUser(user),
              success$: this.facade.userCreated$,
              error$: this.facade.userCreateError$
            } as UserEditDialogConfig
          })
      });
  }

  onEdit(user: User) {
    this.me$.pipe(take(1), mapToAllowedRoles())
      .subscribe((allowedRoles: UserRole[]) => {
        this.dialog
          .open(UserEditDialogComponent, {
            data: {
              type: 'update',
              allowedRoles,
              user,
              onSubmit: (user: User) => this.facade.updateUser(user),
              success$: this.facade.userUpdated$,
              error$: this.facade.userUpdateError$
            }
          })
          .afterClosed()
          .pipe(filter(x => !!x))
          .subscribe((user: User) => this.facade.updateUser(user));
      });
  }

  onDelete(user: User) {
    this.facade.deleteUser(user);
  }

  onDeleteAll(users: User[]) {
    this.facade.deleteUsers(users);
  }

  ngOnDestroy(): void {
    this.facade.unloadUsers();
  }
}
