import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenEditFormComponent } from './access-token-edit-form.component';
import { MatCheckboxModule, MatFormFieldModule, MatInputModule, MatSnackBar } from '@angular/material';
import { UserFacade } from '../../pages/user-page/+state/user.facade';
import { ChangeDetectorRef } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslocoTestingModule } from '@ngneat/transloco';

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
        { provide: MatSnackBar, useValue: {} },
        { provide: UserFacade, useFactory: () => ({}) },
        { provide: ChangeDetectorRef, useFactory: () => ({}) }
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
