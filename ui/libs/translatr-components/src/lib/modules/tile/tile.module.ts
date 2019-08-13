import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TileComponent } from './tile.component';
import { MatCardModule, MatIconModule } from '@angular/material';
import { TileLinkComponent } from './tile-link/tile-link.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [TileComponent, TileLinkComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule
  ],
  exports: [TileComponent, TileLinkComponent]
})
export class TileModule {
}
