import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { By } from '@angular/platform-browser';

/**
 * Regression guard for the Angular / Material 22 (MDC) migration (#230).
 *
 * The admin side-nav renders each entry as `<mat-icon matListItemIcon>` inside a
 * `mat-list-item`:
 *   apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page.component.html
 *
 * Before the migration this used the removed `mat-list-icon` directive, so the
 * leading icons were not placed in the MDC leading slot. This spec pins the row
 * markup to `matListItemIcon` and the leading slot it produces.
 */
@Component({
  standalone: true,
  imports: [MatListModule, MatIconModule],
  template: `
    <mat-nav-list>
      <a mat-list-item>
        <mat-icon matListItemIcon>people</mat-icon>
        Users
      </a>
    </mat-nav-list>
  `
})
class DashboardNavRowHostComponent {}

describe('admin side-nav row markup (MDC)', () => {
  let fixture: ComponentFixture<DashboardNavRowHostComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [DashboardNavRowHostComponent] });
    fixture = TestBed.createComponent(DashboardNavRowHostComponent);
    fixture.detectChanges();
  });

  it('marks the leading icon with the MDC list-item icon class', () => {
    const icon = fixture.debugElement.query(By.css('mat-icon[matListItemIcon]'));
    expect(icon).toBeTruthy();
    expect(icon.nativeElement.classList.contains('mat-mdc-list-item-icon')).toBe(true);
  });

  it('places the leading icon in the MDC leading slot', () => {
    const icon = fixture.debugElement.query(By.css('mat-icon[matListItemIcon]'));
    expect(icon.nativeElement.classList.contains('mdc-list-item__start')).toBe(true);
  });

  it('uses no removed `mat-list-icon` directive', () => {
    expect(fixture.debugElement.query(By.css('[mat-list-icon]'))).toBeNull();
  });
});
