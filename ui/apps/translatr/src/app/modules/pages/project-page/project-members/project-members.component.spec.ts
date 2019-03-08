import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMembersComponent } from './project-members.component';

describe('ProjectMembersComponent', () => {
  let component: ProjectMembersComponent;
  let fixture: ComponentFixture<ProjectMembersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectMembersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMembersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
