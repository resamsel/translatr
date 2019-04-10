import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {User} from "@dev/translatr-sdk";
import {take} from "rxjs/operators";
import {UserDeleted, UserDeleteError} from "../../../../+state/app.actions";

@Component({
  selector: 'dev-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.css']
})
export class DashboardUsersComponent implements OnInit {

  users$ = this.facade.users$;
  userDeleted$ = this.facade.userDeleted$;
  displayedColumns = ['name', 'username', 'email', 'when_created', 'actions'];

  constructor(private readonly facade: AppFacade) {
    facade.loadUsers();
  }

  ngOnInit() {
  }

  onEdit(user: User) {
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
