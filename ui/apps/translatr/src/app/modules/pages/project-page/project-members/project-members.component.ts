import { Component, OnInit } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { openProjectMemberEditDialog } from '../../../shared/project-member-edit-dialog/project-member-edit-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { Project } from '@dev/translatr-model';
import { filter, switchMapTo, take } from 'rxjs/operators';

@Component({
  selector: 'app-project-members',
  templateUrl: './project-members.component.html',
  styleUrls: ['./project-members.component.scss']
})
export class ProjectMembersComponent implements OnInit {
  project$ = this.facade.project$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
  }

  onAdd(project: Project): void {
    console.log('onAdd', project);
    openProjectMemberEditDialog(this.dialog, {projectId: project.id})
      .afterClosed()
      .pipe(filter(x => !!x), switchMapTo(this.project$), take(1))
      .subscribe(p => this.facade.loadProject(p.ownerUsername, p.name));
  }

  onRemove(): void {
    console.log('remove member');
  }
}
