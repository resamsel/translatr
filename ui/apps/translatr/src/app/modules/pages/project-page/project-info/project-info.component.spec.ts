import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInfoComponent } from './project-info.component';
import { ProjectFacade } from '../+state/project.facade';
import {
  MatButtonModule,
  MatCardModule,
  MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatListModule,
  MatProgressBarModule,
  MatSelectModule,
  MatTooltipModule
} from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { WINDOW } from '@translatr/utils';
import { FeatureFlagTestingModule, MetricTestingModule } from '@translatr/components/testing';
import { NavListTestingModule } from '../../../shared/nav-list/testing';
import { ActivityListTestingModule } from '../../../shared/activity-list/testing';
import { ShortNumberModule } from '@dev/translatr-components';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../../+state/app.facade';

describe('ProjectInfoComponent', () => {
  let component: ProjectInfoComponent;
  let fixture: ComponentFixture<ProjectInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectInfoComponent],
      imports: [
        MetricTestingModule,
        NavListTestingModule,
        ActivityListTestingModule,
        ShortNumberModule,
        FeatureFlagTestingModule,

        RouterTestingModule,

        MatDialogModule,
        MatCardModule,
        MatListModule,
        MatIconModule,
        MatProgressBarModule,
        MatButtonModule,
        MatFormFieldModule,
        MatSelectModule,
        MatTooltipModule
      ],
      providers: [
        {
          provide: ProjectFacade,
          useFactory: () => ({
            project$: mockObservable(),
            locales$: mockObservable(),
            keys$: mockObservable(),
            messages$: mockObservable(),
            activities$: mockObservable(),
            accessTokens$: mockObservable()
          })
        },
        {
          provide: AppFacade,
          useFactory: () => ({
            me$: mockObservable()
          })
        },
        { provide: WINDOW, useValue: { location: {} } }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
