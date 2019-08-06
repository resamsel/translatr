import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectMemberEditDialogComponent } from './project-member-edit-dialog.component';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectMemberEditDialogComponent;
  let fixture: ComponentFixture<ProjectMemberEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectMemberEditDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectMemberEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
