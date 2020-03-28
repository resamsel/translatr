import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NotFoundPageComponent } from './not-found-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ErrorPageTestingModule } from '@translatr/components/testing';
import { AppFacade } from '../../../+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('NotFoundPageComponent', () => {
  let component: NotFoundPageComponent;
  let fixture: ComponentFixture<NotFoundPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NotFoundPageComponent],
      imports: [
        ErrorPageTestingModule,

        RouterTestingModule,
        TranslocoTestingModule
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
    fixture = TestBed.createComponent(NotFoundPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
