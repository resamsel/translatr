import { Component, Input } from '@angular/core';
import { Member, Project } from '@dev/translatr-model';

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
})
export class MemberListComponent {
  @Input() project: Project;
  @Input() members: Array<Member>;
}
