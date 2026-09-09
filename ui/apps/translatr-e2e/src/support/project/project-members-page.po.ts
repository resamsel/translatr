import type { Locator, Page } from '@playwright/test';
import { PageObject } from '../page.po';

export class ProjectMembersPage extends PageObject {
  constructor(
    page: Page,
    private readonly username: string,
    private readonly projectName: string,
  ) {
    super(page);
  }

  async navigateTo(): Promise<ProjectMembersPage> {
    await this.page.goto(`${this.username}/${this.projectName}/members`);
    return this;
  }

  getMemberList(): Locator {
    return this.page.locator('app-member-list app-nav-list mat-nav-list');
  }

  getMemberRows(): Locator {
    return this.page.locator('app-member-list a[mat-list-item]');
  }

  getMemberRow(userName: string): Locator {
    return this.page.locator('app-member-list a[mat-list-item]').filter({ hasText: userName });
  }

  getEditButton(userName: string): Locator {
    return this.getMemberRow(userName).locator('button.edit');
  }

  getDeleteButton(userName: string): Locator {
    return this.getMemberRow(userName).locator('confirm-button.delete');
  }

  getTransferButton(userName: string): Locator {
    return this.getMemberRow(userName).locator('button.edit');
  }

  getDialogTitle(): Locator {
    return this.getDialog().locator('[mat-dialog-title]');
  }

  getDialogSaveButton(): Locator {
    return this.getDialog().locator('button[transloco="button.save"]');
  }

  getDialogCancelButton(): Locator {
    return this.getDialog().locator('button[transloco="button.cancel"]');
  }

  async fillMemberUser(search: string): Promise<ProjectMembersPage> {
    await this.getDialog().locator('input').first().fill(search);
    await this.page.getByRole('option', { name: search }).click();
    return this;
  }

  async selectMemberRole(role: string): Promise<ProjectMembersPage> {
    await this.getDialog().locator('mat-select').click();
    await this.page.locator('.mat-mdc-select-panel mat-option').filter({ hasText: role }).click();
    return this;
  }
}
