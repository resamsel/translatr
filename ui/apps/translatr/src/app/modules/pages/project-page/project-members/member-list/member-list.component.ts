import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { Member, Project } from '@dev/translatr-model';
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
  @Input() project: Project;
  @Input() members: Array<Member>;

  @Output() filter = new EventEmitter<string>();

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

  onRemove(member: any): void {
    console.log('remove member', member);
  }

  onFilter(search: string): void {
    this.filter.emit(search);
  }
}
