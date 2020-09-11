import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@ngneat/transloco';
import { ProjectInfographicComponent } from './project-infographic.component';

@NgModule({
  declarations: [ProjectInfographicComponent],
  imports: [CommonModule, TranslocoModule, MatIconModule],
  exports: [ProjectInfographicComponent]
})
export class ProjectInfographicModule {}
