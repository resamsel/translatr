import {Component, Input, OnInit} from '@angular/core';
import {User} from "@dev/translatr-model";
import {ActivatedRoute} from "@angular/router";
import {filter, map, switchMap} from "rxjs/operators";
import {EMPTY, of} from "rxjs";

@Component({
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss']
})
export class AuthBarItemComponent implements OnInit {

  @Input() me: User;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data
      .pipe(
        switchMap((data: { me: User }) => {
          if (data.me === undefined) {
            return this.route.parent ? this.route.parent.data : EMPTY;
          }

          return of(data);
        }),
        map((data: { me: User }) => data.me),
        filter((me: User) => me !== undefined),
      )
      .subscribe((me: User) => this.me = me);
  }
}
