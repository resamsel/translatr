import { Component, OnDestroy } from '@angular/core';
import { AppFacade } from "../../../../+state/app.facade";
import { RequestCriteria, User } from "@dev/translatr-model";
import { map, mapTo, startWith, take } from "rxjs/operators";
import { UserDeleted, UserDeleteError, UsersDeleted, UsersDeleteError } from "../../../../+state/app.actions";
import {
  UserEditDialogComponent,
  UserEditDialogConfig
} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.component";
import { MatDialog, MatSnackBar } from "@angular/material";
import { filter } from "rxjs/internal/operators/filter";
import { UserRole } from "@dev/translatr-model/src/lib/model/user-role";
import { merge, Observable } from "rxjs";
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
  users$ = this.facade.users$;
  allowCreate$ = this.me$.pipe(hasCreateUserPermission());

  load$ = merge(
    this.facade.userDeleted$
      .pipe(filter((action: UserDeleted | UserDeleteError) => action instanceof UserDeleted)),
    this.facade.usersDeleted$
      .pipe(filter((action: UsersDeleted | UsersDeleteError) => action instanceof UsersDeleted))
  ).pipe(
    mapTo({}),
    startWith({limit: '20', order: 'name asc'})
  );

  selected: User[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.facade.userDeleted$
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          snackBar.open(`User ${action.payload.username} has been deleted`, 'Dismiss', {duration: 3000});
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
      });
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as User[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadUsers(criteria);
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
          });
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
