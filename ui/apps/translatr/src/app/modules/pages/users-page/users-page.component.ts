import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { PagedList } from "../../../shared/paged-list";
import { User } from "../../../shared/user";
import { Observable } from "rxjs";

@Component({
  selector: 'app-projects-page',
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit {

  users: PagedList<User>;
  topContributors$: Observable<PagedList<User>>;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { users: PagedList<User> }) => {
        this.users = data.users;
      });
  }
}
