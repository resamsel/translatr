import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';
import { NotFoundPageComponent } from './not-found-page.component';

describe('NotFoundPageComponent', () => {
  let component: NotFoundPageComponent;
  let fixture: ComponentFixture<NotFoundPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(NotFoundPageComponent, {
        remove: { imports: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent] },
        add: { imports: [MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent] }
      }).configureTestingModule({
        imports: [NotFoundPageComponent, RouterTestingModule, TranslocoTestingModule.forRoot({ langs: {}, translocoConfig: { availableLangs: ['en'] } })],
        providers: [
          {
            provide: AppFacade,
            useFactory: () => ({
              loadMe: jest.fn(),
              me$: mockObservable()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(NotFoundPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
