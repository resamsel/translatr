import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ProjectsPageComponent } from './projects-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectsFacade } from './+state/projects.facade';
import { AppFacade } from '../../../+state/app.facade';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { mockObservable } from '@translatr/utils/testing';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { MockFeatureFlagDirective, MockFeatureFlagClassDirective } from '@translatr/components/testing';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { ProjectListComponent } from '../../shared/project-list/project-list.component';
import { MockProjectListComponent, SidenavTestingModule } from '../../testing';
import { MatDialog } from '@angular/material/dialog';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('UsersPageComponent', () => {
  let component: ProjectsPageComponent;
  let fixture: ComponentFixture<ProjectsPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectsPageComponent, {
        remove: { imports: [SidenavModule, ProjectListComponent, FeatureFlagClassDirective] },
        add: { imports: [SidenavTestingModule, MockProjectListComponent, MockFeatureFlagClassDirective] }
      }).configureTestingModule({
        imports: [
          ProjectsPageComponent,

          MockFeatureFlagDirective,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatButtonModule,
          MatTooltipModule,
          MatIconModule
        ],
        providers: [
          {
            provide: ProjectsFacade,
            useFactory: () => ({
              unload$: mockObservable(),
              unloadProjects: jest.fn()
            })
          },
          {
            provide: AppFacade,
            useFactory: () => ({
              me$: mockObservable(),
              queryParams$: mockObservable()
            })
          },
          { provide: MatDialog, useFactory: () => ({}) }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
