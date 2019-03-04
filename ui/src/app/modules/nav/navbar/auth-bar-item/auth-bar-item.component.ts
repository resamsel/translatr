import { Component, OnInit } from '@angular/core';
import { User } from "../../../../shared/user";
import { ActivatedRoute } from "@angular/router";
import { filter, map, switchMap, tap } from "rxjs/operators";
import { of } from "rxjs";

@Component({
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss']
})
export class AuthBarItemComponent implements OnInit {

  me: User;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data
      .pipe(
        switchMap((data: {me: User}) => {
          console.log('route.data', data);
          if (data.me === undefined) {
            return this.route.parent.data;
          }

          return of(data);
        }),
        tap(console.log),
        map((data: {me: User}) => data.me),
        filter((me: User) => me !== undefined),
      )
      .subscribe((me: User) => this.me = me);
  }
}
