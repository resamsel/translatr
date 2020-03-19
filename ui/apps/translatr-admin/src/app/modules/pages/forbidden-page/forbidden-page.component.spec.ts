import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ForbiddenPageComponent } from './forbidden-page.component';
import { ErrorPageTestingModule } from '@translatr/components/testing';
import { AppFacade } from '../../../+state/app.facade';
import { RouterTestingModule } from '@angular/router/testing';

describe('ForbiddenPageComponent', () => {
  let component: ForbiddenPageComponent;
  let fixture: ComponentFixture<ForbiddenPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ForbiddenPageComponent],
      imports: [
        ErrorPageTestingModule,

        RouterTestingModule
      ],
      providers: [
        {
          provide: AppFacade,
          useFactory: () => ({
            loadMe: jest.fn()
          })
        }
      ]
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
