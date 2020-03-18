import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserSettingsComponent } from './user-settings.component';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { mockObservable } from '@translatr/utils/testing';
import { UserFacade } from '../+state/user.facade';

describe('UserSettingsComponent', () => {
  let component: UserSettingsComponent;
  let fixture: ComponentFixture<UserSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserSettingsComponent],
      imports: [
        EmptyViewTestingModule,

        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        NoopAnimationsModule,

        MatDialogModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule
      ],
      providers: [
        { provide: MatSnackBar, useFactory: () => ({}) },
        {
          provide: UserFacade,
          useFactory: () => ({
            user$: mockObservable(),
            error$: mockObservable()
          })
        }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
