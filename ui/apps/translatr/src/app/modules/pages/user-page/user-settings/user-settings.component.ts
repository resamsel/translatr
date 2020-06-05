import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '@dev/translatr-model';
import { findParam } from '@translatr/utils';
import { merge, throwError } from 'rxjs';
import { filter, skip, switchMap } from 'rxjs/operators';
import { UserFacade } from '../+state/user.facade';
import { AbstractEditFormComponent } from '../../../shared/edit-form/abstract-edit-form-component';

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent extends AbstractEditFormComponent<UserSettingsComponent, User>
  implements OnInit {
  user$ = this.facade.user$;
  result$ = merge(
    this.facade.user$.pipe(
      skip(1),
      filter((x) => !!x)
    ),
    this.facade.error$.pipe(
      skip(1),
      filter((x) => !!x),
      switchMap((e) => throwError(e))
    )
  );

  readonly nameFormControl = this.form.get('name');
  readonly usernameFormControl = this.form.get('username');

  constructor(
    readonly fb: FormBuilder,
    readonly snackBar: MatSnackBar,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly facade: UserFacade
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        id: fb.control(''),
        name: fb.control('', [Validators.required, Validators.maxLength(32)]),
        username: fb.control('', [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9_\\.-]*'),
          Validators.maxLength(32)
        ])
      }),
      {},
      undefined,
      (user: User) => {
        facade.updateUser(user);
        return this.result$;
      },
      (i: User) => `${i.name} updated`
    );
  }

  ngOnInit() {
    this.user$.subscribe((i) => this.form.patchValue(i));
  }

  protected onSaved(user: User): void {
    if (user.username !== findParam(this.route.snapshot, 'username')) {
      this.router.navigate(['/', user.username, 'settings']);
    }
  }
}
