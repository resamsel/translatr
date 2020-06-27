import { Injector } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterTestingModule } from '@angular/router/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { GravatarModule } from 'ngx-gravatar';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { UserFacade } from './+state/user.facade';

import { UserPageComponent } from './user-page.component';
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
          useValue: [{ children: [] }]
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
