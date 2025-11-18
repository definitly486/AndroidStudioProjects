#!/bin/sh

GH_FILE=$(ls /usr/local/bin | grep  -x  gh)

if [ -z "$GH_FILE" ]
then
     echo "gh tools не установлен"
     zenity --title="release_apk" --warning --text="gh tools не установлен"  
     exit
fi

APK_PATH="$HOME/AndroidStudioProjects/App_Redmi_A5/app/build/outputs/apk/debug"
cd $HOME/redmia5
mv $APK_PATH/app-debug.apk  $APK_PATH/app_redmi_a5.apk
echo "Y" | gh release  delete-asset apk app_redmi_a5.apk
gh release  upload apk $APK_PATH/app_redmi_a5.apk