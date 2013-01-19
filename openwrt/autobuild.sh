#!/bin/sh

IMAGE_BUILDER="OpenWrt-ImageBuilder-ar71xx_generic-for-linux-i486"
IMAGE_BUILDER_PACKAGE="${IMAGE_BUILDER}.tar.bz2"
IMAGE_BUILDER_LOCATION="http://downloads.openwrt.org/attitude_adjustment/12.09-rc1/ar71xx/generic/"
IMAGE_BUILDER_URL="${IMAGE_BUILDER_LOCATION}${IMAGE_BUILDER_PACKAGE}"

if ! [ -d ${IMAGE_BUILDER} ]; then
    echo "No image builder directory"
    if ![ -f ${IMAGE_BUILDER_PACKAGE} ]; then
        echo "Download package..."
        wget ${IMAGE_BUILDER_URL}
        if [ $? -ne 0 ]; then
            echo "Failed to download ${IMAGE_BUILDER_PACKAGE}"
            exit 1
        fi
        echo "Done"
    fi

    echo "Extract package..."
    tar -xjf ${IMAGE_BUILDER_PACKAGE}
    echo "Done"
fi

echo ${IMAGE_BUILDER}
echo ${IMAGE_BUILDER_LOCATION}
echo ${IMAGE_BUILDER_PACKAGE}
echo ${IMAGE_BUILDER_URL}


PACKAGES=`cat packages_list`
FILES=files
PROFILE=TLWR703

echo ${PACKAGES}

cd ${IMAGE_BUILDER}
make image PROFILE=${PROFILE} FILES=../${FILES} PACKAGES="${PACKAGES}"
cd ..
