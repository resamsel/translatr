import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { FeatureFlagTestingModule, MetricTestingModule, UserCardTestingModule } from '@translatr/components/testing';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { AppFacade } from '../../../../+state/app.facade';

import { DashboardUserComponent } from './dashboard-user.component';

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
      providers: [{ provide: AppFacade, useFactory: () => ({}) }]
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
