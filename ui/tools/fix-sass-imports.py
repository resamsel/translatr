#!/usr/bin/env python3
"""Migrate @import 'mixins'/@import 'theme' to @use in SCSS files."""
import os, re

BASE = '/Users/rene.panzar/Development/Private/translatr/ui'

# Files where @import 'mixins' should become @use 'mixins' as *
MIXINS_FILES = [
    'libs/translatr-components/src/lib/modules/nav/navbar/navbar.component.scss',
    'libs/translatr-components/src/lib/modules/project/project-infographic/project-infographic.component.scss',
    'libs/translatr-components/src/lib/modules/entity/entity-table/entity-table.component.scss',
    'libs/translatr-components/src/lib/modules/empty-view/empty-view.component.scss',
    'apps/translatr/src/app/modules/pages/main-page/main-page.component.scss',
    'apps/translatr/src/app/modules/shared/activity-list/activity-list.component.scss',
    'apps/translatr/src/app/modules/shared/access-token-edit-form/access-token-edit-form.component.scss',
    'apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.scss',
    'apps/translatr/src/app/modules/pages/project-page/project-page.component.scss',
    'apps/translatr/src/app/modules/pages/user-page/user-info/user-info.component.scss',
    'apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-page.component.scss',
    'apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-user/dashboard-user.component.scss',
    'apps/translatr-admin/src/app/modules/nav/sidenav/sidenav.component.scss',
    'libs/translatr-components/src/lib/modules/pages/login-page/login-page.component.scss',
]

# Files with @import 'theme' and/or @import 'mixins' AND map-get to clean up
THEME_FILES = [
    'apps/translatr/src/app/modules/pages/editor-page/editor/editor.component.scss',
    'apps/translatr/src/app/modules/pages/forbidden-page/forbidden-page.component.scss',
    'libs/translatr-components/src/lib/modules/filter-field/filter-field.component.scss',
    'libs/translatr-components/src/lib/modules/pages/error-page/error-page.component.scss',
    'apps/translatr-admin/src/app/modules/pages/dashboard-page/dashboard-feature-flags/dashboard-feature-flags.component.scss',
    'apps/translatr-admin/src/app/modules/pages/forbidden-page/forbidden-page.component.scss',
]

# nav-list: has @import 'theme', map-get, AND #{$i/10} division
NAV_LIST = 'apps/translatr/src/app/modules/shared/nav-list/nav-list.component.scss'

def fix_file(path, content):
    return content

for rel in MIXINS_FILES:
    path = os.path.join(BASE, rel)
    with open(path) as f:
        content = f.read()
    new = content.replace("@import 'mixins';", "@use 'mixins' as *;")
    if new != content:
        with open(path, 'w') as f:
            f.write(new)
        print(f'Fixed mixins: {rel}')

for rel in THEME_FILES:
    path = os.path.join(BASE, rel)
    with open(path) as f:
        content = f.read()
    new = content
    new = new.replace("@import 'theme';", "@use 'theme' as *;")
    new = new.replace("@import 'mixins';", "@use 'mixins' as *;")
    # Remove unused map-get lines (they assign to variables but use hardcoded colors)
    new = re.sub(r'  \$primary: map-get\(\$theme, primary\);\n', '', new)
    new = re.sub(r'  \$accent: map-get\(\$theme, accent\);\n', '', new)
    new = re.sub(r'  \$warning: map-get\(\$theme, warn\);\n', '', new)
    if new != content:
        with open(path, 'w') as f:
            f.write(new)
        print(f'Fixed theme: {rel}')

# Fix nav-list specially: @import 'theme', map-get, and #{$i/10}
path = os.path.join(BASE, NAV_LIST)
with open(path) as f:
    content = f.read()
new = content
new = new.replace("@import 'theme';", "@use 'sass:math';\n@use 'theme' as *;")
new = re.sub(r'  \$warning: map-get\(\$theme, warn\);\n', '', new)
new = new.replace('#{$i/10}s', '#{math.div($i, 10)}s')
if new != content:
    with open(path, 'w') as f:
        f.write(new)
    print(f'Fixed nav-list: {NAV_LIST}')

print('Done.')

