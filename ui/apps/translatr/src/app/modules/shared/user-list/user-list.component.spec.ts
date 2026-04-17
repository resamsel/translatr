import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';

import { UserListComponent } from './user-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserListComponent],
        imports: [
          NavListTestingModule,
          EmptyViewTestingModule,

          RouterTestingModule,
          GravatarModule,
          TimeAgoModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatListModule,
          MatProgressBarModule,
          MatTooltipModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
