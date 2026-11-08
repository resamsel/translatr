import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivityGraphComponent } from '@dev/translatr-components';
import { MockActivityGraphComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { ActivityListComponent } from '../../../shared/activity-list/activity-list.component';
import { MockActivityListComponent } from '../../../shared/activity-list/testing';
import { ProjectActivityComponent } from './project-activity.component';

describe('ProjectActivityComponent', () => {
  let component: ProjectActivityComponent;
  let fixture: ComponentFixture<ProjectActivityComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectActivityComponent, {
        remove: { imports: [ActivityListComponent, ActivityGraphComponent] },
        add: { imports: [MockActivityListComponent, MockActivityGraphComponent] }
      }).configureTestingModule({
        imports: [ProjectActivityComponent],
        providers: [
          {
            provide: ProjectFacade,
            useFactory: () => ({
              activitiesCriteria$: mockObservable(),
              project$: mockObservable(),
              activityAggregated$: mockObservable(),
              unload$: mockObservable()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectActivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
