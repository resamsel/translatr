import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInfographicComponent } from './project-infographic.component';

describe('ProjectInfographicComponent', () => {
  let component: ProjectInfographicComponent;
  let fixture: ComponentFixture<ProjectInfographicComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectInfographicComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInfographicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
