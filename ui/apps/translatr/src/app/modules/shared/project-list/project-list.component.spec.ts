import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent
} from '@dev/translatr-components';
import { MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';

import { ProjectListComponent } from './project-list.component';
import { NavListComponent } from '../nav-list/nav-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectEmptyViewComponent } from '../project-empty-view/project-empty-view.component';
import { ProjectEmptyViewTestingModule } from '../project-empty-view/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('ProjectListComponent', () => {
  let component: ProjectListComponent;
  let fixture: ComponentFixture<ProjectListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectListComponent, {
        remove: {
          imports: [
            NavListComponent,
            ProjectEmptyViewComponent,
            EmptyViewComponent,
            EmptyViewHeaderComponent,
            EmptyViewContentComponent,
            EmptyViewActionsComponent
          ]
        },
        add: {
          imports: [
            NavListTestingModule,
            ProjectEmptyViewTestingModule,
            MockEmptyViewComponent,
            MockEmptyViewActionsComponent,
            MockEmptyViewContentComponent,
            MockEmptyViewHeaderComponent
          ]
        }
      }).configureTestingModule({
        imports: [
          ProjectListComponent,

          RouterTestingModule,

          MatListModule,
          MatProgressBarModule,
          MatTooltipModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
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
