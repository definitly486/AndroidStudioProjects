#!/bin/sh

GH_FILE=$(ls /usr/local/bin | grep  -x  gh)

if [ -z "$GH_FILE" ]
then
     echo "gh tools не установлен"
     zenity --title="release_apk" --warning --text="gh tools не установлен"  
     exit
fi

GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

APK_PATH="$HOME/AndroidStudioProjects/App_Redmi_A5/app/build/outputs/apk/debug"
cd $HOME/AndroidStudioProjects
mv $APK_PATH/app-debug.apk  $APK_PATH/app_redmi_a5-$GIT_BRANCH.apk
echo "Y" | gh release  delete-asset apk app_redmi_a5-$GIT_BRANCH.apk
gh release  upload apk $APK_PATH/app_redmi_a5-$GIT_BRANCH.apk