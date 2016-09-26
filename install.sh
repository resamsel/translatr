# Install location may be overridden by using the TRANSLATR_PREFIX env variable
TRANSLATR_PREFIX=${TRANSLATR_PREFIX:=/usr/local}
TRANSLATR=${TRANSLATR_PREFIX}/bin/translatr

curl -fsSL https://raw.githubusercontent.com/resamsel/translatr/master/translatr/src/python/translatr.py > ${TRANSLATR}

chmod +x ${TRANSLATR}

echo ${TRANSLATR} created and made executable

${TRANSLATR} -h
