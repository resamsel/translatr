import { Pipe, PipeTransform } from '@angular/core';

const defaultConfig = {
  max: 3,
  ellipsis: ', ...',
  glue: ', '
};

@Pipe({
  name: 'ellipsis',
  pure: false
})
export class EllipsisPipe implements PipeTransform {
  transform(value: any, args?: any): string {
    if (!value) {
      return '';
    }

    const config = {
      ...defaultConfig,
      ...args
    };

    if (value instanceof Array) {
      if (value.length > config.max) {
        return value.splice(0, config.max).join(config.glue) + config.ellipsis;
      } else {
        return value.join(config.glue);
      }
    }

    return '';
  }
}
