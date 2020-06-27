import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { EmptyViewTestingModule } from '@translatr/components/testing';

import { ProjectEmptyViewComponent } from './project-empty-view.component';

describe('ProjectEmptyViewComponent', () => {
  let component: ProjectEmptyViewComponent;
  let fixture: ComponentFixture<ProjectEmptyViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectEmptyViewComponent],
      imports: [EmptyViewTestingModule, MatButtonModule]
    }).compileComponents();
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
