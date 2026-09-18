import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectCardLinkComponent } from './project-card-link.component';
import { ProjectCardComponent } from './project-card.component';
import { ProjectCardTestingModule } from './testing';

describe('ProjectCardLinkComponent', () => {
  let component: ProjectCardLinkComponent;
  let fixture: ComponentFixture<ProjectCardLinkComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectCardLinkComponent, {
        remove: { imports: [ProjectCardComponent] },
        add: { imports: [ProjectCardTestingModule] }
      }).configureTestingModule({
        imports: [ProjectCardLinkComponent, RouterTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectCardLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
