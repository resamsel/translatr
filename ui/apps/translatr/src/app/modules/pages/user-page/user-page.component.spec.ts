import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserPageComponent } from './user-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { GravatarModule } from 'ngx-gravatar';
import { MatIconModule, MatTabsModule } from '@angular/material';
import { Injector } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { UserFacade } from './+state/user.facade';
import { USER_ROUTES } from './user-page.token';

describe('UserPageComponent', () => {
  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserPageComponent],
      imports: [
        SidenavTestingModule,
        FeatureFlagTestingModule,

        RouterTestingModule,
        GravatarModule,

        MatTabsModule,
        MatIconModule
      ],
      providers: [
        { provide: Injector, useFactory: () => ({}) },
        {
          provide: UserFacade,
          useFactory: () => ({})
        },
        { provide: AppFacade, useFactory: () => ({}) },
        {
          provide: USER_ROUTES,
          useValue: ([
            { children: [] }
          ])
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
