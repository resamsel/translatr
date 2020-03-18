import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LocaleEditDialogComponent } from './locale-edit-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';

describe('LocaleCreationDialogComponent', () => {
  let component: LocaleEditDialogComponent;
  let fixture: ComponentFixture<LocaleEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LocaleEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,

        MatDialogModule,
        MatFormFieldModule,
        MatInputModule
      ],
      providers: [
        { provide: MatSnackBar, useValue: {} },
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { locale: {} } },
        { provide: ProjectFacade, useFactory: () => ({}) }
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
