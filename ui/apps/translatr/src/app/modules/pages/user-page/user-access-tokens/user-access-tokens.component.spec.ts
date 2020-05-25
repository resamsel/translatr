import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAccessTokensComponent } from './user-access-tokens.component';
import { NavListTestingModule } from '../../../shared/nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MomentModule } from 'ngx-moment';
import { EmptyViewTestingModule } from '@translatr/components/testing';
import { UserFacade } from '../+state/user.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('UserAccessTokensComponent', () => {
  let component: UserAccessTokensComponent;
  let fixture: ComponentFixture<UserAccessTokensComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserAccessTokensComponent],
      imports: [
        NavListTestingModule,
        EmptyViewTestingModule,

        RouterTestingModule,
        MomentModule,

        MatListModule,
        MatIconModule,
        MatTooltipModule
      ],
      providers: [
        {
          provide: UserFacade,
          useFactory: () => ({
            criteria$: mockObservable(),
            user$: mockObservable(),
            destroy$: mockObservable()
          })
        },
        { provide: MatDialog, useValue: {} }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAccessTokensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
