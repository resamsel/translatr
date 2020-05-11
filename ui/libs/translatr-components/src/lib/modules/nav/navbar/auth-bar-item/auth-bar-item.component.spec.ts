import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthBarItemComponent } from './auth-bar-item.component';
import { MatButtonModule, MatDividerModule, MatIconModule, MatMenuModule, MatTooltipModule } from '@angular/material';
import { GravatarModule } from 'ngx-gravatar';
import { TranslocoTestingModule } from '@ngneat/transloco';

describe('AuthBarItemComponent', () => {
  let component: AuthBarItemComponent;
  let fixture: ComponentFixture<AuthBarItemComponent>;

  beforeEach(async(() => {
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
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthBarItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
