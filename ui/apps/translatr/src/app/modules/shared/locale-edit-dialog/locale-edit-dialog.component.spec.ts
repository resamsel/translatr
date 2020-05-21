import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LocaleEditDialogComponent } from './locale-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('LocaleEditDialogComponent', () => {
  let component: LocaleEditDialogComponent;
  let fixture: ComponentFixture<LocaleEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatDialogModule,
        MatFormFieldModule,
        MatInputModule
      ],
      providers: [
        { provide: MatSnackBar, useValue: {} },
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { locale: {} } },
        {provide: ProjectFacade, useFactory: () => ({localeModified$: mockObservable()})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocaleEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
