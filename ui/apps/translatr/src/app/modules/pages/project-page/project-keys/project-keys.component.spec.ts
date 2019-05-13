import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectKeysComponent } from './project-keys.component';

describe('ProjectKeysComponent', () => {
  let component: ProjectKeysComponent;
  let fixture: ComponentFixture<ProjectKeysComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectKeysComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectKeysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
