#!/usr/bin/env python3
import sys

def fix_create_effects(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    lines = content.split('\n')
    in_create_effect = False
    paren_depth = 0
    result = []
    for line in lines:
        if 'createEffect(() =>' in line:
            in_create_effect = True
            paren_depth = line.count('(') - line.count(')')
            result.append(line)
            continue
        if in_create_effect:
            paren_depth += line.count('(') - line.count(')')
            if paren_depth <= 0:
                stripped = line.rstrip()
                if stripped.endswith(');'):
                    line = stripped[:-2] + '));'
                in_create_effect = False
                paren_depth = 0
        result.append(line)

    with open(filepath, 'w') as f:
        f.write('\n'.join(result))
    print(f'Fixed {filepath}')

for filepath in sys.argv[1:]:
    fix_create_effects(filepath)

