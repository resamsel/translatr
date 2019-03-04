import { Component, OnInit } from '@angular/core';
import { User } from "../../../../shared/user";
import { Observable } from "rxjs";
import { PagedList } from "../../../../shared/paged-list";
import { ActivatedRoute } from "@angular/router";
import { UserService } from "../../../../services/user.service";
import { Aggregate } from "../../../../shared/aggregate";

@Component({
  selector: 'app-user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent implements OnInit {

  user: User;
  activity$: Observable<PagedList<Aggregate> | undefined>;

  constructor(private readonly route: ActivatedRoute, private readonly userService: UserService) {
  }

  ngOnInit() {
    this.route.parent.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
        this.activity$ = this.userService.activity(data.user.id);
      });
  }
}
