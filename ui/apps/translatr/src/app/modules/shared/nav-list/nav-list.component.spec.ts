import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NavListComponent } from './nav-list.component';
import { ListHeaderComponent } from '../list-header/list-header.component';
import { ListHeaderTestingModule } from '../list-header/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterTestingModule } from '@angular/router/testing';

describe('NavListComponent', () => {
  let component: NavListComponent;
  let fixture: ComponentFixture<NavListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.overrideComponent(NavListComponent, {
        remove: { imports: [ListHeaderComponent] },
        add: { imports: [ListHeaderTestingModule] }
      }).configureTestingModule({
        imports: [
          NavListComponent,

          RouterTestingModule,

          MatListModule,
          MatButtonModule,
          MatIconModule,
          MatCardModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(NavListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
