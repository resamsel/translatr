#!/bin/bash
set -e

# Install location may be overridden by using the TRANSLATR_PREFIX env variable
TRANSLATR_PREFIX=${TRANSLATR_PREFIX:=/usr/local}
# Install a specific release by using the TRANSLATR_TAG env variable (e.g. v3.4.0);
# unset (the default) installs the latest release.
TRANSLATR_TAG=${TRANSLATR_TAG:=}

TRANSLATR=${TRANSLATR_PREFIX}/bin/translatr
REPO=resamsel/translatr

OS="$(uname -s)"
ARCH="$(uname -m)"

case "${OS}" in
	Linux)
		case "${ARCH}" in
			x86_64) ASSET=translatr-linux-x64 ;;
			aarch64|arm64) ASSET=translatr-linux-arm64 ;;
			*) ASSET="" ;;
		esac
		;;
	Darwin)
		case "${ARCH}" in
			x86_64) ASSET=translatr-macos-x64 ;;
			arm64) ASSET=translatr-macos-arm64 ;;
			*) ASSET="" ;;
		esac
		;;
	*)
		ASSET=""
		;;
esac

if [ -z "${ASSET}" ]; then
	echo "Unsupported platform: ${OS} ${ARCH} has no published translatr binary." >&2
	echo "See https://github.com/${REPO}/releases for available platforms (Linux x64/arm64, macOS x64/arm64, Windows x64)." >&2
	exit 1
fi

if [ -n "${TRANSLATR_TAG}" ]; then
	DOWNLOAD_URL="https://github.com/${REPO}/releases/download/${TRANSLATR_TAG}/${ASSET}"
else
	DOWNLOAD_URL="https://github.com/${REPO}/releases/latest/download/${ASSET}"
fi

curl -fsSL "${DOWNLOAD_URL}" -o /tmp/translatr

if [ -w "$(dirname ${TRANSLATR})" ]; then
	mv /tmp/translatr ${TRANSLATR}
else
	echo "sudo required for installing ${TRANSLATR}..."
	sudo mv /tmp/translatr ${TRANSLATR}
fi

chmod +x ${TRANSLATR}

echo ${TRANSLATR} created and made executable

${TRANSLATR} -h
