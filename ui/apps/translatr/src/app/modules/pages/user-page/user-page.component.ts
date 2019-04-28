import { Component, Inject, OnInit } from '@angular/core';
import { ActivatedRoute, Route } from '@angular/router';
import { User } from '@dev/translatr-model';
import { NameIconRoute } from '@translatr/utils';
import { USER_ROUTES } from './user-page.token';

@Component({
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
})
export class UserPageComponent implements OnInit {

  user: User;
  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly route: ActivatedRoute,
    @Inject(USER_ROUTES) private routes: {children: NameIconRoute[]}[]
  ) {
  }

  ngOnInit(): void {
    this.route.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
      });
  }

  routerLink(route: Route) {
    if (route === '') {
      return `/${this.user.username}`;
    }

    return `/${this.user.username}/${route.path}`;
  }
}
