import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatOptionModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { UserEditDialogComponent } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { UserEditFormTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';

describe('UserEditDialogComponent', () => {
  let component: UserEditDialogComponent;
  let fixture: ComponentFixture<UserEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserEditDialogComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        UserEditFormTestingModule,

        MatFormFieldModule,
        MatDialogModule,
        MatInputModule,
        MatOptionModule,
        MatSelectModule
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
    fixture = TestBed.createComponent(UserEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
