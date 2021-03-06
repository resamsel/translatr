import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Entity, UserEditDialogComponent, UserEditDialogConfig } from '@dev/translatr-components';
import { Feature, RequestCriteria, User, UserRole } from '@dev/translatr-model';
import {
  hasCreateUserPermission,
  hasDeleteAllUsersPermission,
  hasDeleteUserPermission,
  hasEditUserPermission,
  isAdmin
} from '@dev/translatr-sdk';
import { merge, Observable } from 'rxjs';
import { filter, map, mapTo, startWith, take, takeUntil } from 'rxjs/operators';
import {
  UserDeleted,
  UserDeleteError,
  UsersDeleted,
  UsersDeleteError
} from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';

export const mapToAllowedRoles = () =>
  map((me?: User): UserRole[] => [UserRole.User, ...(isAdmin(me) ? [UserRole.Admin] : [])]);

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
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
    this.facade.userDeleted$.pipe(
      filter((action: UserDeleted | UserDeleteError) => action instanceof UserDeleted)
    ),
    this.facade.usersDeleted$.pipe(
      filter((action: UsersDeleted | UsersDeleteError) => action instanceof UsersDeleted)
    )
  ).pipe(mapTo({}), startWith({ limit: '20', order: 'whenCreated desc', fetch: 'count' }));

  selected: User[] = [];

  readonly Feature = Feature;

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    readonly snackBar: MatSnackBar
  ) {
    this.facade.userDeleted$
      .pipe(takeUntil(this.facade.unloadUsers$))
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          snackBar.open(`User ${action.payload.username} has been deleted`, 'Dismiss', {
            duration: 3000
          });
        } else {
          snackBar.open(`User could not be deleted: ${action.payload.error.error}`, 'Dismiss', {
            duration: 8000
          });
        }
      });
    this.facade.usersDeleted$
      .pipe(takeUntil(this.facade.unloadUsers$))
      .subscribe((action: UsersDeleted | UsersDeleteError) => {
        if (action instanceof UsersDeleted) {
          snackBar.open(`${action.payload.length} users have been deleted`, 'Dismiss', {
            duration: 3000
          });
        } else {
          snackBar.open(`Users could not be deleted: ${action.payload.error.error}`, 'Dismiss', {
            duration: 8000
          });
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
    this.me$.pipe(take(1), mapToAllowedRoles()).subscribe((allowedRoles: UserRole[]) => {
      this.dialog.open(UserEditDialogComponent, {
        data: {
          type: 'create',
          allowedRoles,
          onSubmit: (user: User) => this.facade.createUser(user),
          success$: this.facade.userCreated$,
          error$: this.facade.userCreateError$
        } as UserEditDialogConfig
      });
    });
  }

  onEdit(user: User) {
    this.me$.pipe(take(1), mapToAllowedRoles()).subscribe((allowedRoles: UserRole[]) => {
      this.dialog.open(UserEditDialogComponent, {
        data: {
          type: 'update',
          allowedRoles,
          user,
          onSubmit: (u: User) => this.facade.updateUser(u),
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
