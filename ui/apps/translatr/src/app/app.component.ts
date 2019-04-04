import {AfterViewInit, Component, Inject, OnInit, Renderer2, TemplateRef} from '@angular/core';
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent implements AfterViewInit {
  constructor(
    @Inject(DOCUMENT) private readonly document: Document,
    private readonly renderer: Renderer2
  ) {
  }

  ngAfterViewInit(): void {
    this.renderer.removeClass(this.document.body, 'loading');
  }
}
