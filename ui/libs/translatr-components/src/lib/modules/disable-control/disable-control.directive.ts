import { AfterViewInit, Directive, Input } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[disableControl]'
})
export class DisableControlDirective implements AfterViewInit {
  private _disabled = false;

  @Input() set disableControl(disabled: boolean) {
    this._disabled = disabled;
    this.updateControl(disabled);
  }

  constructor(private readonly ngControl: NgControl) {
  }

  ngAfterViewInit(): void {
    this.updateControl(this._disabled);
  }

  private updateControl(disabled: boolean) {
    const action = disabled ? 'disable' : 'enable';
    this.ngControl.control[action]();
  }
}
