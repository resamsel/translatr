import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmButtonComponent } from '@dev/translatr-components';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

describe('ConfirmButtonComponent', () => {
  let component: ConfirmButtonComponent;
  let fixture: ComponentFixture<ConfirmButtonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ConfirmButtonComponent],
      imports: [
        MatMenuModule,
        MatButtonModule,
        MatTooltipModule,
        MatIconModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
