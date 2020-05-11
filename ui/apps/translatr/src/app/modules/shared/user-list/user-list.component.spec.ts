import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserListComponent } from './user-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule, MatProgressBarModule, MatTooltipModule } from '@angular/material';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserListComponent],
      imports: [
        NavListTestingModule,

        RouterTestingModule,
        GravatarModule,
        MomentModule,
        TranslocoTestingModule,

        MatListModule,
        MatProgressBarModule,
        MatTooltipModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
