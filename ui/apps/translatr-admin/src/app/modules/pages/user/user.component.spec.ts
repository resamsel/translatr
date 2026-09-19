import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { MetricComponent, UserCardComponent } from '@dev/translatr-components';
import {
  MockMetricComponent,
  MockUserCardComponent
} from '@translatr/components/testing';
import { FeatureFlagFacade } from '@dev/translatr-model';
import { GravatarModule } from 'ngx-gravatar';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageComponent } from '../../admin-page/admin-page.component';
import { MockAdminPageComponent } from '../../admin-page/testing';

import { UserComponent } from './user.component';

describe('UserComponent', () => {
  let component: UserComponent;
  let fixture: ComponentFixture<UserComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(UserComponent, {
        remove: { imports: [AdminPageComponent, MetricComponent, UserCardComponent] },
        add: { imports: [MockAdminPageComponent, MockMetricComponent, MockUserCardComponent] }
      }).configureTestingModule({
        imports: [
          UserComponent,

          RouterTestingModule,
          GravatarModule,

          MatTooltipModule,
          MatIconModule
        ],
        providers: [
          { provide: AppFacade, useFactory: () => ({}) },
          { provide: FeatureFlagFacade, useFactory: () => ({ hasFeatures$: () => of(false) }) }
        ]
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
