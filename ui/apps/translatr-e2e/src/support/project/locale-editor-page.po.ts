import type { Locator, Page } from '@playwright/test';
import { EditorPage } from './editor-page.po';
import { ProjectLocalesPage } from './project-locales-page.po';

export class LocaleEditorPage extends EditorPage {
  constructor(
    page: Page,
    public readonly username: string,
    public readonly projectName: string,
    public readonly localeName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<LocaleEditorPage> {
    await this.page.goto(`${this.username}/${this.projectName}/locales/${this.localeName}`);
    return this;
  }

  async navigateToLocales(): Promise<ProjectLocalesPage> {
    return new ProjectLocalesPage(this.page, this.username, this.projectName).navigateTo();
  }

  getSelectedLocaleField(): Locator {
    return this.page.locator('.selector .selected-locale');
  }
}
