import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ProjectEditDialogComponent } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('ProjectEditDialogComponent', () => {
  let component: ProjectEditDialogComponent;
  let fixture: ComponentFixture<ProjectEditDialogComponent>;

  beforeEach(
    waitForAsync(() => {
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
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
