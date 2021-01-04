import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ActivityGraphTestingModule } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { ProjectFacade } from '../../../shared/project-state';
import { ActivityListTestingModule } from '../../../shared/activity-list/testing';
import { ProjectActivityComponent } from './project-activity.component';

describe('ProjectActivityComponent', () => {
  let component: ProjectActivityComponent;
  let fixture: ComponentFixture<ProjectActivityComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectActivityComponent],
        imports: [ActivityListTestingModule, ActivityGraphTestingModule],
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
