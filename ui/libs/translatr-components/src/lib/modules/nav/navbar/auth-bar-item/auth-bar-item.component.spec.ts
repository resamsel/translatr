import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoTestingModule } from '@ngneat/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { AuthBarItemComponent } from './auth-bar-item.component';

describe('AuthBarItemComponent', () => {
  let component: AuthBarItemComponent;
  let fixture: ComponentFixture<AuthBarItemComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [AuthBarItemComponent],
        imports: [
          GravatarModule,
          TranslocoTestingModule,

          MatIconModule,
          MatButtonModule,
          MatTooltipModule,
          MatMenuModule,
          MatDividerModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthBarItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
