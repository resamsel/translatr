import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'tile-link',
  templateUrl: './tile-link.component.html',
  styleUrls: ['./tile-link.component.scss'],
  host: {
    class: 'tile'
  }
})
export class TileLinkComponent {
  @Input() link: RouterLink;
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
