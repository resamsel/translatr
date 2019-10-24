import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  constructor(private readonly titleService: Title) {
  }

  setTitle(title: string): void {
    if (!!title) {
      this.titleService.setTitle(`${title} - Translatr`);
    } else {
      this.titleService.setTitle('Translatr');
    }
  }
}
