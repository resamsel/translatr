import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Member, MemberRole, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { filter, switchMapTo, take } from 'rxjs/operators';
import { AppFacade } from '../../../../../+state/app.facade';
import { defaultFilters, FilterCriteria } from '../../../../shared/list-header/list-header.component';
import { openProjectMemberEditDialog } from '../../../../shared/project-member-edit-dialog/project-member-edit-dialog.component';
import { openProjectOwnerEditDialog } from '../../../../shared/project-owner-edit-dialog/project-owner-edit-dialog.component';

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
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

  @Output() edit = new EventEmitter<Member>();
  @Output() delete = new EventEmitter<Member>();

  project$ = this.facade.project$;
  // @ts-ignore
  @HostBinding('style.display') private readonly display = 'block';

  @Input() set members(members: PagedList<Member>) {
    this._members = members;
    this.ownerCount =
      members !== undefined ? members.list.filter(m => m.role === MemberRole.Owner).length : 0;
  }

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
