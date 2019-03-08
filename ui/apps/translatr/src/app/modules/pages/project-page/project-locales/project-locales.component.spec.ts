import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectLocalesComponent } from './project-locales.component';

describe('ProjectLocalesComponent', () => {
  let component: ProjectLocalesComponent;
  let fixture: ComponentFixture<ProjectLocalesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectLocalesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectLocalesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
