import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ForbiddenPageComponent } from './forbidden-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';
import { ErrorPageTestingModule } from '@translatr/components/testing';

describe('NotAllowedPageComponent', () => {
  let component: ForbiddenPageComponent;
  let fixture: ComponentFixture<ForbiddenPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ForbiddenPageComponent],
      imports: [
        RouterTestingModule,
        ErrorPageTestingModule
      ],
      providers: [{
        provide: AppFacade, useFactory: () => ({
          loadMe: jest.fn(),
          me$: mockObservable()
        })
      }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ForbiddenPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
