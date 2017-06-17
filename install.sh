#!/bin/bash

# Install location may be overridden by using the TRANSLATR_PREFIX env variable
TRANSLATR_PREFIX=${TRANSLATR_PREFIX:=/usr/local}
# Install branch/tag may be overridden by using the TRANSLATR_TAG env variable
TRANSLATR_TAG=${TRANSLATR_TAG:=master}

TRANSLATR=${TRANSLATR_PREFIX}/bin/translatr

curl -fsSL https://raw.githubusercontent.com/resamsel/translatr/${TRANSLATR_TAG}/src/python/translatr.py > /tmp/translatr.py

if [ -w "$(basename ${TRANSLATR})" ]; then
	mv /tmp/translatr.py ${TRANSLATR}
else
	echo "sudo required for installing ${TRANSLATR}..."
	sudo mv /tmp/translatr.py ${TRANSLATR}
fi

chmod +x ${TRANSLATR}

echo ${TRANSLATR} created and made executable

if [ "$(which pip)" = "" ]; then
	if [ "$(which easy_install)" = "" ]; then
		sudo apt install python-setuptools
	fi
	echo "sudo required for installing PIP..."
	sudo easy_install pip
fi

if [ "$(pip list | grep pyaml)" = "" -o "$(pip list | grep requests)" = "" ]; then
	echo "sudo required for installing python requirements..."
	sudo pip install pyaml requests tabulate
fi

${TRANSLATR} -h
