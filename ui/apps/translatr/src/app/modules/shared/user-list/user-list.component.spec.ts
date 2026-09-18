import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent,
  UserCardComponent,
  UserCardLinkComponent
} from '@dev/translatr-components';
import {
  MockEmptyViewComponent,
  MockEmptyViewActionsComponent,
  MockEmptyViewContentComponent,
  MockEmptyViewHeaderComponent,
  MockUserCardComponent,
  MockUserCardLinkComponent
} from '@translatr/components/testing';

import { UserListComponent } from './user-list.component';
import { NavListComponent } from '../nav-list/nav-list.component';
import { MockNavListComponent } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoPipe } from '@dev/translatr-components';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserListComponent, {
        remove: {
          imports: [
            NavListComponent,
            UserCardComponent,
            UserCardLinkComponent,
            EmptyViewComponent,
            EmptyViewHeaderComponent,
            EmptyViewContentComponent,
            EmptyViewActionsComponent
          ]
        },
        add: {
          imports: [
            MockNavListComponent,
            MockUserCardComponent,
            MockUserCardLinkComponent,
            MockEmptyViewComponent,
            MockEmptyViewActionsComponent,
            MockEmptyViewContentComponent,
            MockEmptyViewHeaderComponent
          ]
        }
      }).configureTestingModule({
        imports: [
          UserListComponent,

          RouterTestingModule,
          GravatarModule,
          TimeAgoPipe,
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
