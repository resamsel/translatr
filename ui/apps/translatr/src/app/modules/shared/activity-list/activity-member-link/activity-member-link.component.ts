import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Member } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-member-link',
  templateUrl: './activity-member-link.component.html',
  styleUrls: ['./activity-member-link.component.scss']
})
export class ActivityMemberLinkComponent {
  memberLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _member: Member;

  get member(): Member {
    return this._member;
  }

  @Input() set member(member: Member) {
    this._member = member;

    if (!member || !member.projectOwnerUsername || !member.projectName) {
      this.memberLink = undefined;
    } else {
      this.memberLink = ['/', member.projectOwnerUsername, member.projectName, 'members'];
    }
  }
}
