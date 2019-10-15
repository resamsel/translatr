import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  constructor(private readonly titleService: Title) {
  }

  setTitle(title: string): void {
    this.titleService.setTitle(`${title} - Translatr`);
  }
}
