import type { Locator } from '@playwright/test';
import { PageObject } from '../page.po';

/** Shared base for the key editor and locale editor screens. */
export class EditorPage extends PageObject {
  getNavList(): Locator {
    return this.page.locator('.nav-list');
  }

  getNavListItems(): Locator {
    return this.page.locator('.nav-list mat-nav-list a');
  }

  getEditor(): Locator {
    return this.page.locator('.editor');
  }

  getEditorContents(): Locator {
    return this.getEditor().locator('.CodeMirror-code .CodeMirror-line span[role="presentation"]');
  }

  getMeta(): Locator {
    return this.page.locator('.meta');
  }

  getPreviewTab(): Locator {
    return this.page.locator('.meta [role="tab"]:nth-child(1)');
  }

  getPreviewBody(): Locator {
    return this.page.locator('.meta [role="tabpanel"]');
  }

  getPreviewContents(): Locator {
    return this.page.locator('.meta .translation').first();
  }

  getTranslationsTab(): Locator {
    return this.page.locator('.meta [role="tab"]:nth-child(2)');
  }

  getTranslationsBody(): Locator {
    return this.page.locator('.meta [role="tabpanel"]');
  }

  getFilterField(): Locator {
    return this.page.locator('dev-filter-field input');
  }
}
