import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';

import { UserListComponent } from './user-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
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
        EmptyViewTestingModule,

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
