import { Component } from '@angular/core';
import { User, UserService } from "@dev/translatr-sdk";
import { Observable } from "rxjs";

@Component({
  selector: 'dev-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'translatr-admin';
  me: Observable<User | undefined>;

  constructor(private readonly userService: UserService) {
    this.me = userService.getLoggedInUser();
  }
}
