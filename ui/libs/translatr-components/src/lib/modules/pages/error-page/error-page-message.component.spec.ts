import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ErrorPageMessageComponent } from './error-page-message.component';

describe('ErrorPageMessageComponent', () => {
  let component: ErrorPageMessageComponent;
  let fixture: ComponentFixture<ErrorPageMessageComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ErrorPageMessageComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorPageMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
