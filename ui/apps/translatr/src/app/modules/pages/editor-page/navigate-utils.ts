import { PagedList } from '@dev/translatr-model';
import { combineLatest, Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { MessageItem } from './message-item';

export const navigateItems = (
  messageItems$: Observable<PagedList<MessageItem>>,
  selected$: Observable<string>,
  matcher: (messageItem: MessageItem, selected: string) => boolean,
  defaultIndex: (listLength: number) => number,
  nextIndex: (index: number | undefined) => number
): Promise<MessageItem> => {
  return new Promise((resolve, reject) => {
    combineLatest([messageItems$, selected$])
      .pipe(take(1))
      .subscribe(([messageItems, selected]) => {
        if (
          messageItems === undefined ||
          messageItems.list === undefined ||
          messageItems.list.length === 0
        ) {
          return reject();
        }
        if (selected === undefined) {
          const i = defaultIndex(messageItems.list.length);
          return resolve(messageItems.list[i]);
        }
        const index = nextIndex(
          messageItems.list.findIndex((messageItem) => matcher(messageItem, selected))
        );
        if (index !== undefined && index >= 0 && index < messageItems.list.length) {
          return resolve(messageItems.list[index]);
        }

        reject();
      });
  });
};
