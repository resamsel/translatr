# Install location may be overridden by using the TRANSLATR_PREFIX env variable
TRANSLATR_PREFIX=${TRANSLATR_PREFIX:=/usr/local}

curl -fsSL https://raw.githubusercontent.com/resamsel/translatr/master/translatr/src/python/translatr.py > $TRANSLATR_PREFIX/bin/translatr

chmod +x $TRANSLATR_PREFIX/bin/translatr

echo $TRANSLATR_PREFIX/bin/translatr created and made executable

$TRANSLATR_PREFIX/bin/translatr -h
