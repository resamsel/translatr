import { Component, OnInit } from '@angular/core';
import { PagedList } from "../../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import { ActivatedRoute } from "@angular/router";
import { User } from "../../../../../../../../libs/translatr-model/src/lib/model/user";

@Component({
  selector: 'app-dashboard-users',
  templateUrl: './dashboard-users.component.html',
  styleUrls: ['./dashboard-users.component.scss']
})
export class DashboardUsersComponent implements OnInit {

  users: PagedList<User>;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { users: PagedList<User> }) => {
        this.users = data.users;
      });
  }
}
