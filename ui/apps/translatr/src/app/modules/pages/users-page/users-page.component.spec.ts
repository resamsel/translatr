import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UsersPageComponent } from './users-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { UserListComponent } from '../../shared/user-list/user-list.component';
import { UsersFacade } from './+state/users.facade';
import { mockObservable } from '@translatr/utils/testing';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { MockFeatureFlagClassDirective } from '@translatr/components/testing';
import { MockUserListComponent } from '../../shared/user-list/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('UsersPageComponent', () => {
  let component: UsersPageComponent;
  let fixture: ComponentFixture<UsersPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UsersPageComponent, {
        remove: { imports: [SidenavModule, UserListComponent, FeatureFlagClassDirective] },
        add: { imports: [SidenavTestingModule, MockUserListComponent, MockFeatureFlagClassDirective] }
      }).configureTestingModule({
        imports: [
          UsersPageComponent,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ],
        providers: [
          {
            provide: AppFacade,
            useFactory: () => ({
              queryParams$: mockObservable()
            })
          },
          {
            provide: UsersFacade,
            useFactory: () => ({
              unload$: mockObservable(),
              unload: jest.fn()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
