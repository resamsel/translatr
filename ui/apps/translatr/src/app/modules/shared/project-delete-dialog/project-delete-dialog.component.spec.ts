import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectDeleteDialogComponent } from './project-delete-dialog.component';

describe('ProjectDeleteDialogComponent', () => {
  let component: ProjectDeleteDialogComponent;
  let fixture: ComponentFixture<ProjectDeleteDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectDeleteDialogComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectDeleteDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
