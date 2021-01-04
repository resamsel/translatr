import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { KeyListComponent } from './key-list.component';
import { RouterTestingModule } from '@angular/router/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NavListTestingModule } from '../../../../shared/nav-list/testing';
import { ButtonTestingModule, EmptyViewTestingModule } from '@translatr/components/testing';

describe('KeyListComponent', () => {
  let component: KeyListComponent;
  let fixture: ComponentFixture<KeyListComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [KeyListComponent],
        imports: [
          NavListTestingModule,
          ButtonTestingModule,
          EmptyViewTestingModule,

          RouterTestingModule,

          MatDialogModule,
          MatListModule,
          MatIconModule,
          MatProgressBarModule,
          MatTooltipModule,
          MatButtonModule
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
