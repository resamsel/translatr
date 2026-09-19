import { Injector } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { MockFeatureFlagClassDirective } from '@translatr/components/testing';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';
import { MockSidenavComponent } from '../../nav/sidenav/testing';
import { UserFacade } from './+state/user.facade';

import { UserPageComponent } from './user-page.component';
import { USER_ROUTES } from './user-page.token';

describe('UserPageComponent', () => {
  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserPageComponent, {
        remove: { imports: [SidenavComponent, FeatureFlagClassDirective] },
        add: { imports: [MockSidenavComponent, MockFeatureFlagClassDirective] }
      }).configureTestingModule({
        imports: [
          UserPageComponent,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatTabsModule,
          MatIconModule
        ],
        providers: [
          { provide: Injector, useFactory: () => ({}) },
          {
            provide: UserFacade,
            useFactory: () => ({ unload: jest.fn() })
          },
          { provide: AppFacade, useFactory: () => ({}) },
          {
            provide: USER_ROUTES,
            useValue: [{ children: [] }]
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
