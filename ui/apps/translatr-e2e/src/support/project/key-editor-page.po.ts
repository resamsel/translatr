import type { Locator, Page } from '@playwright/test';
import { EditorPage } from './editor-page.po';
import { ProjectKeysPage } from './project-keys-page.po';

export class KeyEditorPage extends EditorPage {
  constructor(
    page: Page,
    public readonly username: string,
    public readonly projectName: string,
    public readonly keyName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<KeyEditorPage> {
    await this.page.goto(`${this.username}/${this.projectName}/keys/${this.keyName}`);
    return this;
  }

  async navigateToKeys(): Promise<ProjectKeysPage> {
    return new ProjectKeysPage(this.page, this.username, this.projectName).navigateTo();
  }

  getSelectedKeyField(): Locator {
    return this.page.locator('.selector input.selected-key');
  }
}
