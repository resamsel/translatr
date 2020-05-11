import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UsersPageComponent } from './users-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { UsersFacade } from './+state/users.facade';
import { mockObservable } from '@translatr/utils/testing';
import { SidenavTestingModule } from '../../nav/sidenav/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { UserListTestingModule } from '../../shared/user-list/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('UsersPageComponent', () => {
  let component: UsersPageComponent;
  let fixture: ComponentFixture<UsersPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UsersPageComponent],
      imports: [
        SidenavTestingModule,
        FeatureFlagTestingModule,
        UserListTestingModule,

        RouterTestingModule,
        TranslocoTestingModule
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
            unload$: mockObservable()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
