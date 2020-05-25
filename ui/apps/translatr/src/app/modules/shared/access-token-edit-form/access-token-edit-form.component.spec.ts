import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenEditFormComponent } from './access-token-edit-form.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserFacade } from '../../pages/user-page/+state/user.facade';
import { ChangeDetectorRef } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { mockObservable } from '@translatr/utils/testing';

describe('AccessTokenEditFormComponent', () => {
  let component: AccessTokenEditFormComponent;
  let fixture: ComponentFixture<AccessTokenEditFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AccessTokenEditFormComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TranslocoTestingModule,

        MatFormFieldModule,
        MatInputModule,
        MatCheckboxModule
      ],
      providers: [
        {provide: MatSnackBar, useValue: {}},
        {provide: UserFacade, useFactory: () => ({accessTokenModified$: mockObservable()})},
        {provide: ChangeDetectorRef, useFactory: () => ({})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
