import { Component, OnInit } from '@angular/core';
import { PagedList } from "../../../../shared/paged-list";
import { ActivatedRoute } from "@angular/router";
import { User } from "../../../../shared/user";

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
        console.log(data);
        this.users = data.users;
      });
  }
}
