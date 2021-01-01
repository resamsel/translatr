import { ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Identifiable } from '@dev/translatr-model';
import { Observable, ReplaySubject, Subject } from 'rxjs';
import { skip } from 'rxjs/operators';
import { BaseEditFormComponent } from './base-edit-form.component';

interface DummyEntity extends Identifiable {}

class DummyEditFormComponent extends BaseEditFormComponent<DummyEditFormComponent, DummyEntity> {
  constructor(
    snackBar: MatSnackBar,
    dialogRef: MatDialogRef<DummyEditFormComponent, DummyEntity>,
    form: FormGroup,
    data: Partial<DummyEntity>,
    create: (r: DummyEntity) => void,
    update: (r: DummyEntity) => void,
    result$: Observable<[DummyEntity, undefined] | [undefined, any]>,
    messageProvider: (r: DummyEntity) => string,
    changeDetectorRef: ChangeDetectorRef
  ) {
    super(
      snackBar,
      dialogRef,
      form,
      data,
      create,
      update,
      result$,
      messageProvider,
      changeDetectorRef
    );
  }
}

describe('BaseEditFormComponent', () => {
  let snackBar: MatSnackBar & {
    open: jest.Mock;
  };
  let dialogRef: MatDialogRef<DummyEditFormComponent, DummyEntity>;
  let form: FormGroup;
  let create: jest.Mock;
  let update: jest.Mock;
  let result$: Subject<[DummyEntity, undefined] | [undefined, any]>;
  let messageProvider: (r: DummyEntity) => string & jest.Mock;
  let changeDetectorRef: ChangeDetectorRef;

  const createComponent = (data: Partial<DummyEntity>) =>
    new DummyEditFormComponent(
      snackBar,
      dialogRef,
      form,
      data,
      create,
      update,
      result$,
      messageProvider,
      changeDetectorRef
    );

  beforeEach(() => {
    snackBar = { open: jest.fn() } as MatSnackBar & {
      open: jest.Mock;
    };
    dialogRef = { close: jest.fn() } as MatDialogRef<DummyEditFormComponent, DummyEntity> & {
      close: jest.Mock;
    };
    form = new FormGroup({ id: new FormControl('') });
    create = jest.fn();
    update = jest.fn();
    result$ = new ReplaySubject<[DummyEntity, undefined] | [undefined, any]>(3);
    messageProvider = jest.fn();
    changeDetectorRef = {} as ChangeDetectorRef;

    result$.next([undefined, undefined]);
  });

  it('should create component', () => {
    // given
    const data: Partial<DummyEntity> = {};

    // when
    const actual = createComponent(data);

    // then
    expect(actual).toBeDefined();
  });

  it('should trigger create', () => {
    // given
    const data: Partial<DummyEntity> = {};
    const target = createComponent(data);

    // when
    target.onSave();

    // then
    expect(create.mock.calls).toHaveLength(1);
    expect(create.mock.calls[0][0]).toEqual({ ...data, id: '' });
  });

  it('should trigger update', () => {
    // given
    const data: Partial<DummyEntity> = { id: '1' };
    const target = createComponent(data);

    // when
    target.onSave();

    // then
    expect(update.mock.calls).toHaveLength(1);
    expect(update.mock.calls[0][0]).toEqual(data);
  });

  it('should not show snackbar message when not processing', done => {
    // given
    const data: Partial<DummyEntity> = { id: '1' };
    createComponent(data);

    // when
    result$.next([data, undefined]);

    // then
    result$.pipe(skip(1)).subscribe(() => {
      expect(snackBar.open.mock.calls).toHaveLength(0);
      done();
    });
  });
});
