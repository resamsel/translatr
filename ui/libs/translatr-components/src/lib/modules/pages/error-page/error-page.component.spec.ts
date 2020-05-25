import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorPageComponent } from './error-page.component';
import { ErrorPageHeaderComponent } from '@translatr/translatr-components/src/lib/modules/pages/error-page/error-page-header.component';
import { ErrorPageMessageComponent } from '@translatr/translatr-components/src/lib/modules/pages/error-page/error-page-message.component';
import { MatIconModule } from '@angular/material/icon';

describe('ErrorPageComponent', () => {
  let component: ErrorPageComponent;
  let fixture: ComponentFixture<ErrorPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ErrorPageComponent,
        ErrorPageHeaderComponent,
        ErrorPageMessageComponent
      ],
      imports: [
        MatIconModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
