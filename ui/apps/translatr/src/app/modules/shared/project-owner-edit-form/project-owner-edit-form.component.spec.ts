import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOwnerEditFormComponent } from './project-owner-edit-form.component';

describe('ProjectMemberEditFormComponent', () => {
  let component: ProjectOwnerEditFormComponent;
  let fixture: ComponentFixture<ProjectOwnerEditFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectOwnerEditFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
