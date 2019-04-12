import {Component} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {User} from "@dev/translatr-sdk";
import {map, take} from "rxjs/operators";
import {UserDeleted, UserDeleteError} from "../../../../+state/app.actions";
import {
  UserEditDialogComponent,
  UserEditDialogConfig
} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.component";
import {MatDialog} from "@angular/material";
import {filter} from "rxjs/internal/operators/filter";
import {UserRole} from "@dev/translatr-sdk/src/lib/shared/user-role";
import {Observable} from "rxjs";

const isAdmin = (user?: User): boolean => user !== undefined && user.role === UserRole.Admin;

const hasCreateUserPermission = () => map(isAdmin);

const hasEditUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id === user.id) || isAdmin(me));

const hasDeleteUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id !== user.id) && isAdmin(me));

const mapToAllowedRoles = () => map((me?: User): UserRole[] =>
  [UserRole.User, ...isAdmin(me) ? [UserRole.Admin] : []]);

@Component({
  selector: 'dev-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.css']
})
export class DashboardUsersComponent {

  readonly displayedColumns = ['name', 'role', 'username', 'email', 'when_created', 'actions'];

  me$ = this.facade.me$;
  users$ = this.facade.users$;
  userDeleted$ = this.facade.userDeleted$;
  allowCreate$ = this.me$.pipe(hasCreateUserPermission());

  constructor(private readonly facade: AppFacade, private readonly dialog: MatDialog) {
    facade.loadUsers();
  }

  allowEdit$(user: User): Observable<boolean> {
    return this.me$.pipe(hasEditUserPermission(user));
  }

  allowDelete$(user: User): Observable<boolean> {
    return this.me$.pipe(hasDeleteUserPermission(user));
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
    this.userDeleted$
      .pipe(take(1))
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          console.log(`User ${action.payload.username} has been deleted`);
        } else {
          console.warn(`User ${action.payload.error.error} could not be deleted`);
        }
      });
    this.facade.deleteUser(user);
  }
}
