import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EmptyViewTestingModule } from '@translatr/components/testing';

import { ProjectListComponent } from './project-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectEmptyViewTestingModule } from '../project-empty-view/testing';

describe('ProjectListComponent', () => {
  let component: ProjectListComponent;
  let fixture: ComponentFixture<ProjectListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProjectListComponent],
        imports: [
          NavListTestingModule,
          ProjectEmptyViewTestingModule,
          EmptyViewTestingModule,

          RouterTestingModule,

          MatListModule,
          MatProgressBarModule,
          MatTooltipModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
