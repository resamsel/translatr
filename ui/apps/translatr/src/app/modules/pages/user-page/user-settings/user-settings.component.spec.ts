import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { UserFacade } from '../+state/user.facade';

import { UserSettingsComponent } from './user-settings.component';

describe('UserSettingsComponent', () => {
  let component: UserSettingsComponent;
  let fixture: ComponentFixture<UserSettingsComponent>;

  beforeEach(
    waitForAsync(() => {
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
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
