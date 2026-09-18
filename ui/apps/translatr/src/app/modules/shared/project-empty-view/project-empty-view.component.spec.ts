import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';

import { ProjectEmptyViewComponent } from './project-empty-view.component';

describe('ProjectEmptyViewComponent', () => {
  let component: ProjectEmptyViewComponent;
  let fixture: ComponentFixture<ProjectEmptyViewComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectEmptyViewComponent],
        imports: [MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent, MatButtonModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEmptyViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
