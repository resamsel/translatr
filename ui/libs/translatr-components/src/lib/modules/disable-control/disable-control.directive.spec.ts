import { NgControl } from '@angular/forms';
import { DisableControlDirective } from './disable-control.directive';

interface ControlMock {
  control: {
    enable: jest.Mock;
    disable: jest.Mock;
  };
}

describe('DisableControlDirective', () => {
  it('should create an instance', () => {
    const directive = new DisableControlDirective(undefined);
    expect(directive).toBeTruthy();
  });

  it('should invoke default state on control on ngAfterViewInit', () => {
    const mockControl: NgControl & ControlMock = ({
      control: {
        enable: jest.fn(),
        disable: jest.fn()
      }
    } as unknown) as NgControl & ControlMock;
    const target = new DisableControlDirective(mockControl);

    // when
    target.ngAfterViewInit();

    // then
    expect(mockControl.control.enable.mock.calls.length).toEqual(1);
    expect(mockControl.control.disable.mock.calls.length).toEqual(0);
  });

  it('should invoke enable on control', () => {
    const mockControl: NgControl & ControlMock = ({
      control: {
        enable: jest.fn(),
        disable: jest.fn()
      }
    } as unknown) as NgControl & ControlMock;
    const target = new DisableControlDirective(mockControl);

    // when
    target.disableControl = false;

    // then
    expect(mockControl.control.enable.mock.calls.length).toEqual(1);
    expect(mockControl.control.disable.mock.calls.length).toEqual(0);
  });

  it('should invoke disable on control', () => {
    const mockControl: NgControl & ControlMock = ({
      control: {
        enable: jest.fn(),
        disable: jest.fn()
      }
    } as unknown) as NgControl & ControlMock;
    const target = new DisableControlDirective(mockControl);

    // when
    target.disableControl = true;

    // then
    expect(mockControl.control.enable.mock.calls.length).toEqual(0);
    expect(mockControl.control.disable.mock.calls.length).toEqual(1);
  });
});
