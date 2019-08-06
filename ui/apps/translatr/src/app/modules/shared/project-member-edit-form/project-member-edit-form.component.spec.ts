import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMemberEditFormComponent } from './project-member-edit-form.component';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectMemberEditFormComponent;
  let fixture: ComponentFixture<ProjectMemberEditFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectMemberEditFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
