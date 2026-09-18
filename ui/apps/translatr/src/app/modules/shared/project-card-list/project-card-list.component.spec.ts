import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NavListComponent } from '../nav-list/nav-list.component';
import { ProjectCardComponent } from '../project-card/project-card.component';
import { ProjectCardLinkComponent } from '../project-card/project-card-link.component';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';
import {
  NavListTestingModule,
  ProjectCardTestingModule,
  ProjectEmptyViewTestingModule
} from '../testing';
import { ProjectCardListComponent } from './project-card-list.component';

describe('ProjectCardListComponent', () => {
  let component: ProjectCardListComponent;
  let fixture: ComponentFixture<ProjectCardListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectCardListComponent, {
        remove: {
          imports: [NavListComponent, ProjectCardComponent, ProjectCardLinkComponent, ProjectEmptyViewComponent]
        },
        add: {
          imports: [NavListTestingModule, ProjectCardTestingModule, ProjectEmptyViewTestingModule]
        }
      }).configureTestingModule({
        imports: [ProjectCardListComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectCardListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
