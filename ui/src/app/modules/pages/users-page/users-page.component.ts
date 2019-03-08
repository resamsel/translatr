import { Component, OnInit } from '@angular/core';
import {Observable} from "rxjs";
import {PagedList} from "../../../shared/paged-list";
import {User} from "../../../shared/user";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-users-page',
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit {

  users$: Observable<PagedList<User>>;

  constructor(private readonly userService: UserService) { }

  ngOnInit() {
    this.users$ = this.userService.getUsers();
  }
}
