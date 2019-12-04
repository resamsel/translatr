import { ChangeDetectorRef, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { Member, MemberRole, memberRoles, User } from '@dev/translatr-model';
import { Subject } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { debounceTime, map, takeUntil } from 'rxjs/operators';
import { MemberService } from '@translatr/translatr-sdk/src/lib/services/member.service';

interface MemberForm {
  id?: number;
  projectId: string;
  user: User;
  role: MemberRole;
}

const memberToUser = (member: Member): User => {
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
  extends AbstractEditFormComponent<ProjectMemberEditFormComponent, MemberForm, Member>
  implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  @Input() set member(member: Member) {
    this.updateValue(member);
  }

  @Input() users: User[];
  @Input() dialogRef: MatDialogRef<any, Member>;
  @Input() canModifyOwner = false;

  @Output() userFilter = new EventEmitter<string | undefined>();

  roles = memberRoles;

  readonly userFormControl = this.form.get('user');

  constructor(
    readonly memberService: MemberService,
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
      (form: MemberForm) => memberService.create(formToMember(form)),
      (form: MemberForm) => memberService.update(formToMember(form)),
      (member: Member) => `${member.userName} is now ${member.role}`
    );
  }

  ngOnInit(): void {
    this.failure.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.changeDetectorRef.markForCheck());
    this.userFormControl.valueChanges
      .pipe(
        debounceTime(200),
        map(value => typeof value === 'string' ? value : value.username),
        takeUntil(this.destroy$)
      )
      .subscribe(value => this.userFilter.emit(value));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  displayFn(user?: User): string | undefined {
    return user ? user.username : undefined;
  }

  get dirty(): boolean {
    return this.form.dirty;
  }

  private updateValue(member: Member): void {
    this.form.patchValue({
      id: member.id,
      projectId: member.projectId,
      user: memberToUser(member),
      role: member.role
    });
  }
}
