/**
 * True when `url` targets this app's own API: a relative URL, or an absolute
 * URL whose resolved origin matches the page's origin. Used to keep SDK
 * interceptors from touching third-party requests (auth-provider redirects,
 * CDNs).
 */
export const isSameOriginApiRequest = (url: string): boolean => {
  if (!/^https?:\/\//i.test(url) && !url.startsWith('//')) {
    return true;
  }

  if (typeof window === 'undefined' || !window.location) {
    return false;
  }

  try {
    return new URL(url, window.location.origin).origin === window.location.origin;
  } catch {
    return false;
  }
};
