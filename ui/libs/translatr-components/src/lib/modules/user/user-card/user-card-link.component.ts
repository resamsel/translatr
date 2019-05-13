import { Component, Input, OnInit } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'app-user-card-link',
  templateUrl: './user-card-link.component.html',
  styleUrls: ['./user-card-link.component.scss']
})
export class UserCardLinkComponent implements OnInit {
  @Input() user: User;

  constructor() {}

  ngOnInit() {}
}
