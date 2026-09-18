import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import {
  EmptyViewComponent,
  EmptyViewActionsComponent,
  EmptyViewContentComponent,
  EmptyViewHeaderComponent
} from '@dev/translatr-components';
import { MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

import { ProjectEmptyViewComponent } from './project-empty-view.component';

describe('ProjectEmptyViewComponent', () => {
  let component: ProjectEmptyViewComponent;
  let fixture: ComponentFixture<ProjectEmptyViewComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ProjectEmptyViewComponent, {
        remove: {
          imports: [EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent]
        },
        add: {
          imports: [MockEmptyViewComponent, MockEmptyViewActionsComponent, MockEmptyViewContentComponent, MockEmptyViewHeaderComponent]
        }
      }).configureTestingModule({
        imports: [
          ProjectEmptyViewComponent,
          MatButtonModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })
        ]
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
