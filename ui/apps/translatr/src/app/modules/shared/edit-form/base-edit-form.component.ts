import { ChangeDetectorRef, HostListener, Injectable, OnDestroy } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  ConstraintViolation,
  ConstraintViolationErrorInfo,
  Error,
  Identifiable
} from '@dev/translatr-model';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

const ENTER_KEYCODE = 'Enter';

const getViolations = (
  error: Error | ConstraintViolationErrorInfo | undefined
): ConstraintViolation[] | undefined => {
  if (error !== undefined) {
    if ('error' in error) {
      return error.error.violations;
    }

    return error.violations;
  }
  return undefined;
};

@Injectable()
export abstract class BaseEditFormComponent<T, F extends Identifiable, R extends Identifiable = F>
  implements OnDestroy {
  processing = false;

  protected readonly destroy$ = new Subject<void>();

  protected constructor(
    protected readonly snackBar: MatSnackBar,
    protected readonly dialogRef: MatDialogRef<T, R>,
    readonly form: FormGroup,
    protected readonly data: Partial<F>,
    protected readonly create: (r: F) => void,
    protected readonly update: (r: F) => void,
    protected readonly result$: Observable<[R, undefined] | [undefined, any]>,
    protected readonly messageProvider: (r: R) => string,
    protected readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.form.patchValue(data);
    result$
      .pipe(
        filter(([result, error]) => this.processing && (Boolean(result) || Boolean(error))),
        takeUntil(this.destroy$)
      )
      .subscribe(([r, error]: [R, { error: Error }]) => {
        if (error !== undefined) {
          return this.onError(error.error);
        }
        return this.onSuccess(r);
      });
  }

  get invalid(): boolean {
    return this.form.invalid;
  }

  public onSave(): void {
    this.processing = true;

    const value: F = this.form.value;

    if (value.id) {
      this.update(value);
    } else {
      this.create(value);
    }
  }

  protected isValid(): boolean {
    return this.form.valid;
  }

  protected onSuccess(r: R): void {
    this.processing = false;
    this.onSaved(r);

    this.snackBar.open(this.messageProvider(r), 'Dismiss', { duration: 3000 });
  }

  protected onSaved(r: R): void {
    if (this.dialogRef !== undefined) {
      this.dialogRef.close(r);
    }
  }

  protected onError(error: Error | ConstraintViolationErrorInfo | undefined): void {
    this.processing = false;
    const violations = getViolations(error);
    if (violations !== undefined) {
      violations.forEach((violation: ConstraintViolation) => {
        const control = this.form.get(violation.field);
        if (!!control) {
          control.setErrors({ violation: violation.message });
          control.markAsTouched();
        }
      });
    }
    this.onInvalid(error);
  }

  protected onInvalid(error: Error | ConstraintViolationErrorInfo | undefined): void {
    if (this.changeDetectorRef !== undefined) {
      this.changeDetectorRef.markForCheck();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:keyup', ['$event'])
  protected onHotkey(event: KeyboardEvent) {
    if (
      event.key === ENTER_KEYCODE &&
      event.ctrlKey === true &&
      this.isValid() &&
      !this.processing
    ) {
      this.onSave();
    }
  }
}
