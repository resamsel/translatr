import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectEditDialogComponent } from '@dev/translatr-components';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { mockObservable } from '@translatr/utils/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('ProjectEditDialogComponent', () => {
  let component: ProjectEditDialogComponent;
  let fixture: ComponentFixture<ProjectEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatFormFieldModule,
        MatDialogModule,
        MatInputModule
      ],
      providers: [
        {
          provide: MatDialogRef,
          useFactory: () => ({
            afterClosed: () => mockObservable()
          })
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            success$: mockObservable(),
            error$: mockObservable()
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
