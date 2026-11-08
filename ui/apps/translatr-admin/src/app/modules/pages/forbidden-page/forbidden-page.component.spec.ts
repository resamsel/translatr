import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent } from '@dev/translatr-components';
import { MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent } from '@translatr/components/testing';
import { AppFacade } from '../../../+state/app.facade';
import { ForbiddenPageComponent } from './forbidden-page.component';

describe('ForbiddenPageComponent', () => {
  let component: ForbiddenPageComponent;
  let fixture: ComponentFixture<ForbiddenPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(ForbiddenPageComponent, {
        remove: { imports: [ErrorPageComponent, ErrorPageHeaderComponent, ErrorPageMessageComponent] },
        add: { imports: [MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent] }
      }).configureTestingModule({
        imports: [ForbiddenPageComponent, RouterTestingModule],
        providers: [
          {
            provide: AppFacade,
            useFactory: () => ({
              loadMe: jest.fn()
            })
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ForbiddenPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
