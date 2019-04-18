import {Component, Input, OnInit} from '@angular/core';
import {Project} from "../../../../../../../../../libs/translatr-model/src/lib/model/project";
import {Member} from "../../../../../../../../../libs/translatr-model/src/lib/model/member";

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
})
export class MemberListComponent implements OnInit {

  @Input() project: Project;
  @Input() members: Array<Member>;

  constructor() {
  }

  ngOnInit() {
  }

}
