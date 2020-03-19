import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarComponent } from './navbar.component';
import { TitleService } from '@translatr/utils/src/lib/services/title.service';
import { MatButtonModule, MatIconModule, MatToolbarModule, MatTooltipModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NavbarComponent],
      imports: [
        RouterTestingModule,

        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule
      ],
      providers: [
        {
          provide: TitleService,
          useFactory: () => ({
            setTitle: jest.fn()
          })
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
