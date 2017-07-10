# --- !Ups

update user_
  set username = md5(random()::text)
  where username in ('projects', 'users', 'profile', 'activity', 'login', 'logout', 'authenticate',
    'command', 'api', 'javascriptRoutes', 'assets');
update project
  set name = md5(random()::text)
  where name in ('projects', 'activity', 'linkedAccounts', 'accessTokens', 'create', 'edit',
    'settings');

# --- !Downs
