import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { FeatureFlagTestingModule, MetricTestingModule } from '@translatr/components/testing';
import { AppFacade } from '../../../../+state/app.facade';
import { DashboardInfoComponent } from './dashboard-info.component';

describe('DashboardInfoComponent', () => {
  let component: DashboardInfoComponent;
  let fixture: ComponentFixture<DashboardInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardInfoComponent],
      imports: [
        FeatureFlagTestingModule,
        MetricTestingModule,
        ShortNumberModule,

        RouterTestingModule
      ],
      providers: [
        {
          provide: AppFacade,
          useFactory: () => ({
            loadUsers: jest.fn(),
            loadProjects: jest.fn(),
            loadAccessTokens: jest.fn(),
            loadActivities: jest.fn()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
