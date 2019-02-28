import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../../../../shared/user";
import {tap} from "rxjs/operators";

@Component({
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss']
})
export class AuthBarItemComponent implements OnInit {
  me$: Observable<User>;

  constructor(private readonly http: HttpClient) {
  }

  ngOnInit() {
    this.me$ = this.http.get<User>('/api/me').pipe(tap(console.log));
  }
}
