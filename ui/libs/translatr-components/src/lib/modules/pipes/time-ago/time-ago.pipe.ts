import { Pipe, PipeTransform, OnDestroy, ChangeDetectorRef, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

@Pipe({
  standalone: false,
  name: 'amTimeAgo',
  pure: false,
})
export class TimeAgoPipe implements PipeTransform, OnDestroy {
  private timer: ReturnType<typeof setInterval> | null = null;
  private lastValue: string = '';

  constructor(private cdRef: ChangeDetectorRef) {}

  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }

    const date = value instanceof Date ? value : new Date(value);
    this.lastValue = this.getTimeAgo(date);
    this.removeTimer();
    this.timer = setInterval(() => {
      this.lastValue = this.getTimeAgo(date);
      this.cdRef.markForCheck();
    }, 60000);

    return this.lastValue;
  }

  private getTimeAgo(date: Date): string {
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (seconds < 30) return 'a few seconds ago';
    if (seconds < 90) return 'a minute ago';

    const minutes = Math.floor(seconds / 60);
    if (minutes < 45) return `${minutes} minutes ago`;

    const hours = Math.floor(minutes / 60);
    if (hours < 1.5) return 'an hour ago';
    if (hours < 22) return `${hours} hours ago`;

    const days = Math.floor(hours / 24);
    if (days < 1.5) return 'a day ago';
    if (days < 26) return `${days} days ago`;

    const months = Math.floor(days / 30);
    if (months < 1.5) return 'a month ago';
    if (months < 12) return `${months} months ago`;

    const years = Math.floor(months / 12);
    if (years < 1.5) return 'a year ago';
    return `${years} years ago`;
  }

  ngOnDestroy(): void {
    this.removeTimer();
  }

  private removeTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}

@NgModule({
  declarations: [TimeAgoPipe],
  exports: [TimeAgoPipe],
  imports: [CommonModule],
})
export class TimeAgoModule {}

