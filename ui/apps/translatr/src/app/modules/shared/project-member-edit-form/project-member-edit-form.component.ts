import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Inject,
  Input,
  OnInit,
  Output
} from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Member, MemberRole, memberRoles, User } from '@dev/translatr-model';
import { debounceTime, map, takeUntil } from 'rxjs/operators';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';
import { ProjectFacade } from '../project-state/+state';

interface MemberForm {
  id?: number;
  projectId: string;
  user: User;
  role: MemberRole;
}

const memberToUser = (member: Partial<Member>): User => {
  return {
    id: member.userId,
    name: member.userName,
    username: member.userUsername
  };
};

const formToMember = (form: MemberForm): Member => {
  return {
    id: form.id,
    projectId: form.projectId,
    role: form.role,
    userId: form.user.id
  };
};

@Component({
  selector: 'app-project-member-edit-form',
  templateUrl: './project-member-edit-form.component.html',
  styleUrls: ['./project-member-edit-form.component.scss']
})
export class ProjectMemberEditFormComponent
  extends BaseEditFormComponent<ProjectMemberEditFormComponent, MemberForm, Member>
  implements OnInit {
  @Input() set member(member: Partial<Member>) {
    this.updateValue(member);
  }

  @Input() users: User[];
  @Input() dialogRef: MatDialogRef<any, Member>;
  @Input() canModifyOwner = false;

  @Output() userFilter = new EventEmitter<string | undefined>();

  roles = memberRoles;

  readonly userFormControl = this.form.get('user');
  readonly roleFormControl = this.form.get('role');

  constructor(
    readonly facade: ProjectFacade,
    readonly snackBar: MatSnackBar,
    readonly changeDetectorRef: ChangeDetectorRef,
    readonly fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) readonly data: Member
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        id: [''],
        projectId: [''],
        user: [{}],
        role: ['']
      }),
      data,
      (form: MemberForm) => facade.createMember(formToMember(form)),
      (form: MemberForm) => facade.updateMember(formToMember(form)),
      facade.memberModified$,
      (member: Member) => `${member.userName} is now ${member.role}`,
      changeDetectorRef
    );
  }

  ngOnInit(): void {
    this.userFormControl.valueChanges
      .pipe(
        debounceTime(200),
        map(value => (typeof value === 'string' ? value : value.username)),
        takeUntil(this.destroy$)
      )
      .subscribe(value => this.userFilter.emit(value));
  }

  displayFn(user?: User): string | undefined {
    return user ? user.username : undefined;
  }

  private updateValue(member: Partial<Member>): void {
    this.form.patchValue({
      id: member.id,
      projectId: member.projectId,
      user: memberToUser(member),
      role: member.role
    });
  }
}
