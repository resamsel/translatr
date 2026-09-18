import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent } from '@translatr/components/testing';
import { mockObservable } from '@translatr/utils/testing';
import { AppFacade } from '../../../+state/app.facade';

import { ForbiddenPageComponent } from './forbidden-page.component';

describe('NotAllowedPageComponent', () => {
  let component: ForbiddenPageComponent;
  let fixture: ComponentFixture<ForbiddenPageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ForbiddenPageComponent],
        imports: [RouterTestingModule, MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent],
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
    fixture = TestBed.createComponent(ForbiddenPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
