import { ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { Member, MemberRole, memberRoles, User } from '@dev/translatr-model';
import { Observable, Subject } from 'rxjs';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AbstractControl, FormBuilder } from '@angular/forms';
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

  @Output() userFilter = new EventEmitter<string | undefined>();

  filteredOptions: Observable<User[]>;
  roles = memberRoles;

  constructor(
    readonly memberService: MemberService,
    readonly snackBar: MatSnackBar,
    readonly changeDetectorRef: ChangeDetectorRef,
    readonly fb: FormBuilder
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
      {},
      (form: MemberForm) => memberService.create(formToMember(form)),
      (form: MemberForm) => memberService.update(formToMember(form)),
      (member: Member) => `${member.userName} is now ${member.role}`
    );
  }

  ngOnInit(): void {
    this.error.pipe(takeUntil(this.destroy$))
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

  public get userFormControl(): AbstractControl {
    return this.form.get('user');
  }

  private updateValue(member: Member): void {
    this.form.patchValue({
      projectId: member.projectId,
      user: memberToUser(member),
      role: member.role
    });
  }
}
