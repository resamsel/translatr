import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../shared/user";

@Component({
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
})
export class UserPageComponent implements OnInit {

  user: User;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.data
      .subscribe((data: { user: User }) => {
        this.user = data.user;
        console.log('page data', data);
      });
  }
}
