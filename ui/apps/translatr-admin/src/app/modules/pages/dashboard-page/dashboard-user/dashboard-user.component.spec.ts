import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardUserComponent } from './dashboard-user.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../../+state/app.facade';
import { FeatureFlagTestingModule, MetricTestingModule, UserCardTestingModule } from '@translatr/components/testing';
import { GravatarModule } from 'ngx-gravatar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MomentModule } from 'ngx-moment';
import { ShortNumberModule } from '@dev/translatr-components';

describe('DashboardUserComponent', () => {
  let component: DashboardUserComponent;
  let fixture: ComponentFixture<DashboardUserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardUserComponent],
      imports: [
        FeatureFlagTestingModule,
        UserCardTestingModule,
        MetricTestingModule,
        ShortNumberModule,

        RouterTestingModule,
        GravatarModule,
        MomentModule,

        MatTooltipModule,
        MatIconModule
      ],
      providers: [
        { provide: AppFacade, useFactory: () => ({}) }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
