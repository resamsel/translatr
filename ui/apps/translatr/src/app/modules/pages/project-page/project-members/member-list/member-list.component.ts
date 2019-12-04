import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { Member, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { openProjectMemberEditDialog } from '../../../../shared/project-member-edit-dialog/project-member-edit-dialog.component';
import { filter, switchMapTo, take } from 'rxjs/operators';
import { ProjectFacade } from '../../+state/project.facade';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
})
export class MemberListComponent {
  memberList: PagedList<Member>;
  @Input() criteria: RequestCriteria;

  @Input() project: Project;

  private _members: Array<Member>;

  get members(): Array<Member> {
    return this._members;
  }

  @Input() canCreate = false;
  @Input() canDelete = false;

  @Input() set members(members: Array<Member>) {
    this._members = members;
    this.memberList = {
      list: members,
      total: members.length,
      hasNext: false,
      hasPrev: false,
      limit: -1,
      offset: 0
    };
  }

  @Output() filter = new EventEmitter<string>();
  @Output() delete = new EventEmitter<Member>();

  project$ = this.facade.project$;
  // @ts-ignore
  @HostBinding('style.display') private readonly display = 'block';

  constructor(
    private readonly facade: ProjectFacade,
    private readonly dialog: MatDialog
  ) {
  }

  onAdd(project: Project): void {
    openProjectMemberEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(filter(x => !!x), switchMapTo(this.project$), take(1))
      .subscribe(p => this.facade.loadProject(p.ownerUsername, p.name));
  }


  onDelete(member: Member): void {
    this.delete.emit(member);
  }

  onFilter(search: string): void {
    this.filter.emit(search);
  }
}
