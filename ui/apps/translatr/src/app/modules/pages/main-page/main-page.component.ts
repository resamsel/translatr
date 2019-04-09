import { Component } from '@angular/core';
import { User } from "../../../../../../../libs/translatr-sdk/src/lib/shared/user";
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.scss']
})
export class MainPageComponent {

  me: User;

  constructor(private readonly route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data
      .subscribe((data: { me: User }) => {
        this.me = data.me;
      });
  }
}
