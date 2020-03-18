import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectCardListComponent } from './project-card-list.component';
import { NavListTestingModule, ProjectCardTestingModule, ProjectEmptyViewTestingModule } from '../testing';

describe('ProjectCardListComponent', () => {
  let component: ProjectCardListComponent;
  let fixture: ComponentFixture<ProjectCardListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectCardListComponent],
      imports: [
        NavListTestingModule,
        ProjectCardTestingModule,
        ProjectEmptyViewTestingModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectCardListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
