import { Observable } from 'rxjs';
import { MatDialogRef, MatSnackBar } from '@angular/material';
import { take } from 'rxjs/operators';
import { ConstraintViolation, Error, Locale } from '@dev/translatr-model';
import { HostListener } from '@angular/core';
import { FormGroup } from '@angular/forms';

const ENTER_KEYCODE = 'Enter';

export abstract class AbstractCreationDialogComponent<T, R> {

  processing = false;
  log = console.log;

  constructor(
    protected readonly snackBar: MatSnackBar,
    protected readonly dialogRef: MatDialogRef<T, R>,
    protected readonly form: FormGroup,
    protected readonly creator: (r: R) => Observable<R>,
    protected readonly messageProvider: (r: R) => string
  ) {
  }

  public onSave(): void {
    this.processing = true;

    this.create()
      .pipe(take(1))
      .subscribe(
        (r: R) => this.onCreated(r),
        (res: { error: Error }) => this.onError(res.error)
      );
  }

  protected isValid(): boolean {
    return this.form.valid;
  }

  protected create(): Observable<R> {
    return this.creator(this.form.value);
  }

  protected onCreated(r: R): void {
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
