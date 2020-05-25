import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyEditDialogComponent } from './key-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('KeyEditDialogComponent', () => {
  let component: KeyEditDialogComponent;
  let fixture: ComponentFixture<KeyEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KeyEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatFormFieldModule,
        MatInputModule
      ],
      providers: [
        {provide: MatSnackBar, useValue: {}},
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: {key: {}}},
        {provide: ProjectFacade, useFactory: () => ({keyModified$: mockObservable()})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
