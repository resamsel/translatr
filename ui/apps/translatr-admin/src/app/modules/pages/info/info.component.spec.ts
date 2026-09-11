import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { ActivityService } from '@dev/translatr-sdk';
import {
  ActivityGraphTestingModule,
  FeatureFlagTestingModule,
  MetricTestingModule
} from '@translatr/components/testing';
import { of } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { AdminPageTestingModule } from '../../admin-page/testing';
import { InfoComponent } from './info.component';

describe('InfoComponent', () => {
  let component: InfoComponent;
  let fixture: ComponentFixture<InfoComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [InfoComponent],
        imports: [
          ActivityGraphTestingModule,
          AdminPageTestingModule,
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
          },
          {
            provide: ActivityService,
            useValue: { aggregated: () => of({ list: [] }) }
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(InfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
