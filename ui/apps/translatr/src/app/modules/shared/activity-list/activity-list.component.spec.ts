import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EmptyViewComponent, EmptyViewHeaderComponent } from '@dev/translatr-components';
import { MockEmptyViewComponent, MockEmptyViewHeaderComponent } from '@translatr/components/testing';
import { ActivityListComponent } from './activity-list.component';
import { NavListComponent } from '../nav-list/nav-list.component';
import { NavListTestingModule } from '../nav-list/testing';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { TimeAgoPipe } from '@dev/translatr-components';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';

describe('ActivityListComponent', () => {
  let component: ActivityListComponent;
  let fixture: ComponentFixture<ActivityListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ActivityListComponent, {
        remove: { imports: [NavListComponent, EmptyViewComponent, EmptyViewHeaderComponent] },
        add: { imports: [NavListTestingModule, MockEmptyViewComponent, MockEmptyViewHeaderComponent] }
      }).configureTestingModule({
        imports: [
          ActivityListComponent,

          RouterTestingModule,
          TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } }),

          MatListModule,
          MatIconModule,
          MatTooltipModule,

          TimeAgoPipe
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
