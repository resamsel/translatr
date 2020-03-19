import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { UserEditDialogComponent } from '@dev/translatr-components';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
  MatFormFieldModule,
  MatInputModule,
  MatOptionModule,
  MatSelectModule
} from '@angular/material';
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
