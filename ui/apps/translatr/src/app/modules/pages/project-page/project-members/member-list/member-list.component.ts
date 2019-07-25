import { Component, Input } from '@angular/core';
import { Member, Project, User, UserRole } from '@dev/translatr-model';

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
})
export class MemberListComponent {
  @Input() project: Project;
  @Input() members: Array<Member>;

  asUser(member: Member): User {
    return {
      id: member.userId,
      name: member.userName,
      username: member.userUsername,
      email: member.userEmail,

      role: UserRole.User
    };
  }

  onAdd(): void {
    console.log('add member');
  }

  onRemove(): void {
    console.log('remove member');
  }
}
