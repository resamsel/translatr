import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ProjectCardLinkComponent } from './project-card-link.component';
import { MockProjectCardComponent } from './testing';

describe('ProjectCardLinkComponent', () => {
  let component: ProjectCardLinkComponent;
  let fixture: ComponentFixture<ProjectCardLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectCardLinkComponent, MockProjectCardComponent],
      imports: [RouterTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectCardLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
