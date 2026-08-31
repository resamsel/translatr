import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatListModule } from '@angular/material/list';
import { By } from '@angular/platform-browser';

/**
 * Regression guard for the Angular / Material 22 (MDC) migration (#230).
 *
 * The editor key / locale lists render each row as `<h3 matListItemTitle>` +
 * `<p matListItemLine>` inside a `mat-list-item`:
 *   apps/translatr/src/app/modules/pages/editor-page/locale-editor-page.component.html
 *   apps/translatr/src/app/modules/pages/editor-page/key-editor-page.component.html
 *
 * Before the migration these used the removed `matLine` directive, which left
 * the title and subtitle overlapping in the editor's left-hand list. This spec
 * pins the row markup to the MDC line directives and the slots they produce so
 * a future upgrade that renames them fails loudly instead of regressing the UI.
 */
@Component({
  standalone: true,
  imports: [MatListModule],
  template: `
    <mat-nav-list>
      <a mat-list-item class="key">
        <h3 matListItemTitle>key.name</h3>
        <p class="sub-title translation" matListItemLine>translation value</p>
      </a>
    </mat-nav-list>
  `
})
class EditorListRowHostComponent {}

describe('editor key/locale list row markup (MDC)', () => {
  let fixture: ComponentFixture<EditorListRowHostComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [EditorListRowHostComponent] });
    fixture = TestBed.createComponent(EditorListRowHostComponent);
    fixture.detectChanges();
  });

  it('places the title in the MDC primary-text slot', () => {
    const title = fixture.debugElement.query(By.css('h3[matListItemTitle]'));
    expect(title).toBeTruthy();
    expect(title.nativeElement.classList.contains('mdc-list-item__primary-text')).toBe(true);
  });

  it('places the subtitle in the MDC secondary-text slot', () => {
    const line = fixture.debugElement.query(By.css('p[matListItemLine]'));
    expect(line).toBeTruthy();
    expect(line.nativeElement.classList.contains('mdc-list-item__secondary-text')).toBe(true);
  });

  it('uses no removed `matLine` directive', () => {
    expect(fixture.debugElement.query(By.css('[matLine]'))).toBeNull();
  });
});
