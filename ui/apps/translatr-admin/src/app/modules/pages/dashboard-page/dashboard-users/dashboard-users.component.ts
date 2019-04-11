import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {User} from "@dev/translatr-sdk";
import {take} from "rxjs/operators";
import {UserDeleted, UserDeleteError} from "../../../../+state/app.actions";
import {UserEditDialogComponent} from "@dev/translatr-components/src/lib/modules/user/user-edit-dialog/user-edit-dialog.component";
import {MatDialog} from "@angular/material";
import {filter} from "rxjs/internal/operators/filter";

@Component({
  selector: 'dev-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.css']
})
export class DashboardUsersComponent {

  users$ = this.facade.users$;
  userDeleted$ = this.facade.userDeleted$;
  displayedColumns = ['name', 'username', 'email', 'when_created', 'actions'];

  constructor(private readonly facade: AppFacade, private readonly dialog: MatDialog) {
    facade.loadUsers();
  }

  onCreate() {
    this.dialog
      .open(UserEditDialogComponent, {
        data: {
          type: 'create',
          user: {}
        }
      })
      .afterClosed()
      .pipe(filter(x => !!x))
      .subscribe((user: User) => {
        console.log('The dialog was closed', user);
        this.facade.createUser(user);
      });
  }

  onEdit(user: User) {
    this.dialog
      .open(UserEditDialogComponent, {
        data: {
          type: 'update',
          user
        }
      })
      .afterClosed()
      .pipe(filter(x => !!x))
      .subscribe((user: User) => {
        console.log('The dialog was closed', user);
        this.facade.updateUser(user);
      });
  }

  onDelete(user: User) {
    this.userDeleted$
      .pipe(take(1))
      .subscribe((action: UserDeleted | UserDeleteError) => {
        if (action instanceof UserDeleted) {
          console.log(`User ${action.payload.username} has been deleted`);
        } else {
          console.warn(`User ${action.payload.username} could not be deleted`);
        }
      });
    this.facade.deleteUser(user);
  }
}
