import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectEmptyViewComponent } from './project-empty-view.component';

describe('ProjectEmptyViewComponent', () => {
  let component: ProjectEmptyViewComponent;
  let fixture: ComponentFixture<ProjectEmptyViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectEmptyViewComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEmptyViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
