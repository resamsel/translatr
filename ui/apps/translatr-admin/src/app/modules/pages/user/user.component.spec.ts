import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import {
  FeatureFlagTestingModule,
  MetricTestingModule,
  UserCardTestingModule
} from '@translatr/components/testing';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '@dev/translatr-components';
import { AppFacade } from '../../../+state/app.facade';

import { UserComponent } from './user.component';

describe('UserComponent', () => {
  let component: UserComponent;
  let fixture: ComponentFixture<UserComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [UserComponent],
        imports: [
          FeatureFlagTestingModule,
          UserCardTestingModule,
          MetricTestingModule,
          ShortNumberModule,

          RouterTestingModule,
          GravatarModule,
          TimeAgoModule,

          MatTooltipModule,
          MatIconModule
        ],
        providers: [{ provide: AppFacade, useFactory: () => ({}) }]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
