import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyListComponent } from './key-list.component';
import { RouterTestingModule } from '@angular/router/testing';
import { MatButtonModule, MatDialogModule, MatIconModule, MatListModule, MatProgressBarModule, MatTooltipModule } from '@angular/material';
import { NavListTestingModule } from '../../../../shared/nav-list/testing';
import { ButtonTestingModule, EmptyViewTestingModule } from '@translatr/components/testing';

describe('KeyListComponent', () => {
  let component: KeyListComponent;
  let fixture: ComponentFixture<KeyListComponent>;

  beforeEach(async(() => {
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
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
