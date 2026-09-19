import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostBinding, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterModule } from '@angular/router';
import { ConfirmButtonComponent, TimeAgoPipe } from '@dev/translatr-components';
import { Member, MemberRole, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { filter, switchMapTo, take } from 'rxjs/operators';
import { GravatarModule } from 'ngx-gravatar';
import { AppFacade } from '../../../../../+state/app.facade';
import { NavListComponent } from '../../../../shared/nav-list/nav-list.component';
import {
  defaultFilters,
  FilterCriteria
} from '../../../../shared/list-header/list-header.component';
import { openProjectMemberEditDialog } from '../../../../shared/project-member-edit-dialog/project-member-edit-dialog.component';
import { openProjectOwnerEditDialog } from '../../../../shared/project-owner-edit-dialog/project-owner-edit-dialog.component';

@Component({
  standalone: true,
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./member-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    TranslocoModule,
    NavListComponent,
    ConfirmButtonComponent,
    GravatarModule,
    MatListModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    TimeAgoPipe
  ]
})
export class MemberListComponent {
  ownerCount: number;

  @Input() criteria: RequestCriteria;
  @Input() project: Project;

  @Output() filter = new EventEmitter<FilterCriteria>();
  filters = defaultFilters;

  @Input() canCreate = false;
  @Input() canDelete = false;
  @Input() canModifyOwner = false;
  @Input() canTransferOwnership = false;

  private _members: PagedList<Member>;

  get members(): PagedList<Member> {
    return this._members;
  }

  @Input() set members(members: PagedList<Member>) {
    this._members = members;
    this.ownerCount =
      members !== undefined ? members.list.filter(m => m.role === MemberRole.Owner).length : 0;
  }

  @Output() edit = new EventEmitter<Member>();
  @Output() delete = new EventEmitter<Member>();

  project$ = this.facade.project$;
  @HostBinding('style.display') protected readonly display = 'block';

  // filters = [
  //   ...defaultFilters,
  //   {
  //     key: 'roles',
  //     type: 'string',
  //     title: 'Role',
  //     value: ''
  //   }
  // ];

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    private readonly dialog: MatDialog
  ) {}

  onAdd(project: Project): void {
    openProjectMemberEditDialog(this.dialog, { projectId: project.id }, this.canModifyOwner)
      .afterClosed()
      .pipe(
        filter(x => !!x),
        switchMapTo(this.project$),
        take(1)
      )
      .subscribe(p => this.facade.loadProject(p.ownerUsername, p.name));
  }

  onEdit(member: Member, event: MouseEvent): boolean {
    openProjectMemberEditDialog(this.dialog, member, this.canModifyOwner)
      .afterClosed()
      .pipe(
        filter(x => !!x),
        switchMapTo(this.project$),
        take(1)
      )
      .subscribe(p => this.facade.loadProject(p.ownerUsername, p.name));
    this.edit.emit(member);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(member: Member): void {
    this.delete.emit(member);
  }

  onTransferOwnership(event: MouseEvent): boolean {
    openProjectOwnerEditDialog(this.dialog, this.project)
      .afterClosed()
      .pipe(filter(x => !!x))
      .subscribe(p => this.router.navigate(['/', p.ownerUsername, p.name, 'members']));
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onFilter(criteria: FilterCriteria): void {
    this.filter.emit(criteria);
  }
}
