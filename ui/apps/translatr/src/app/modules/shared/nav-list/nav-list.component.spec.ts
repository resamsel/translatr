import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavListComponent } from './nav-list.component';
import { ListHeaderTestingModule } from '../list-header/testing';
import { MatButtonModule, MatCardModule, MatIconModule, MatListModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';

describe('NavListComponent', () => {
  let component: NavListComponent;
  let fixture: ComponentFixture<NavListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NavListComponent],
      imports: [
        ListHeaderTestingModule,

        RouterTestingModule,

        MatListModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
