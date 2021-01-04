import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProjectInfographicComponent } from './project-infographic.component';

describe('ProjectInfographicComponent', () => {
  let component: ProjectInfographicComponent;
  let fixture: ComponentFixture<ProjectInfographicComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectInfographicComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInfographicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
