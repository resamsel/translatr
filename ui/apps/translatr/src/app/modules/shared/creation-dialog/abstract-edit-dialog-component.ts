import { Observable } from 'rxjs';
import { MatDialogRef, MatSnackBar } from '@angular/material';
import { take } from 'rxjs/operators';
import { ConstraintViolation, Error, Locale } from '@dev/translatr-model';
import { HostListener, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';

const ENTER_KEYCODE = 'Enter';

export abstract class AbstractEditDialogComponent<T, R extends { id?: number | string }>
  implements OnInit {

  processing = false;
  log = console.log;

  constructor(
    protected readonly snackBar: MatSnackBar,
    protected readonly dialogRef: MatDialogRef<T, R>,
    protected readonly form: FormGroup,
    protected readonly data: Partial<R>,
    protected readonly create: (r: R) => Observable<R>,
    protected readonly update: (r: R) => Observable<R>,
    protected readonly messageProvider: (r: R) => string
  ) {
  }

  ngOnInit(): void {
    this.form.setValue(this.data);
  }

  public onSave(): void {
    this.processing = true;

    const value: R = this.form.value;
    const consume$ = value.id ? this.update(value) : this.create(value);
    consume$
      .pipe(take(1))
      .subscribe(
        (r: R) => this.onSuccess(r),
        (res: { error: Error }) => this.onError(res.error)
      );
  }

  protected isValid(): boolean {
    return this.form.valid;
  }

  protected onSuccess(r: R): void {
    this.processing = false;
    this.dialogRef.close(r);
    this.snackBar.open(
      this.messageProvider(r),
      'Dismiss',
      { duration: 3000 }
    );
  }

  protected onError(error: Error): void {
    this.processing = false;
    error.error.violations.forEach((violation: ConstraintViolation) => {
      const control = this.form.get(violation.field);
      if (!!control) {
        control.setErrors({ violation: violation.message });
        control.markAsTouched();
      }
    });
  }

  @HostListener('window:keyup', ['$event'])
  protected onHotkey(event: KeyboardEvent) {
    if (event.key === ENTER_KEYCODE && this.isValid() && !this.processing) {
      this.onSave();
    }
  }
}
