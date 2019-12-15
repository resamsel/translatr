import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOwnerEditDialogComponent } from './project-owner-edit-dialog.component';

describe('ProjectMemberEditDialogComponent', () => {
  let component: ProjectOwnerEditDialogComponent;
  let fixture: ComponentFixture<ProjectOwnerEditDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectOwnerEditDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOwnerEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
