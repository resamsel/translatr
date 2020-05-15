import { async, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { AppFacade } from './+state/app.facade';
import { mockObservable } from '@translatr/utils/testing';

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, TranslocoTestingModule],
      declarations: [AppComponent],
      providers: [
        {
          provide: AppFacade, useFactory: () => ({
            loadMe: jest.fn(),
            me$: mockObservable()
          })
        }
      ]
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
});
