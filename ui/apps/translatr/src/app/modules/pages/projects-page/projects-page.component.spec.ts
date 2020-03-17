import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectsPageComponent } from './projects-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectsFacade } from './+state/projects.facade';
import { AppFacade } from '../../../+state/app.facade';
import { MatButtonModule, MatIconModule, MatTooltipModule } from '@angular/material';
import { mockObservable } from '@translatr/utils/testing';
import { FeatureFlagTestingModule } from '@translatr/components/testing';
import { ProjectListTestingModule, SidenavTestingModule } from '../../testing';
import { MatDialog } from '@angular/material/dialog';

describe('UsersPageComponent', () => {
  let component: ProjectsPageComponent;
  let fixture: ComponentFixture<ProjectsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectsPageComponent],
      imports: [
        SidenavTestingModule,
        FeatureFlagTestingModule,
        ProjectListTestingModule,

        RouterTestingModule,

        MatButtonModule,
        MatTooltipModule,
        MatIconModule
      ],
      providers: [
        {
          provide: ProjectsFacade,
          useFactory: () => ({
            unload$: mockObservable()
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
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
