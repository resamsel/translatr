import { Pipe, PipeTransform } from '@angular/core';

const powers = [
  { key: 'Q', value: Math.pow(10, 15) },
  { key: 'T', value: Math.pow(10, 12) },
  { key: 'B', value: Math.pow(10, 9) },
  { key: 'M', value: Math.pow(10, 6) },
  { key: 'k', value: 1000 }
];

@Pipe({
  name: 'shortNumber'
})
export class ShortNumberPipe implements PipeTransform {

  transform(value: any, ...args: any[]): any {
    if (value === null) {
      return null;
    }

    if (value === 0) {
      return '0';
    }

    const fractionSize = 1;
    const rounder = Math.pow(10, fractionSize);
    const prefix = value < 0 ? '-' : '';

    let abs = Math.abs(value);
    let key = '';
    for (let i = 0; i < powers.length; i++) {
      let reduced = abs / powers[i].value;
      reduced = Math.round(reduced * rounder) / rounder;
      if (reduced >= 1) {
        abs = reduced;
        key = powers[i].key;
        break;
      }
    }

    return prefix + abs + key;
  }
}
