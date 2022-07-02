# Swarogya - The Hospital Management System
[![Webp-net-resizeimage-2.png](https://i.postimg.cc/DydnJ5KW/Webp-net-resizeimage-2.png)](https://postimg.cc/qzR9Wsq0)
[![cool.gif](https://i.postimg.cc/gjW6F6BL/cool.gif)](https://postimg.cc/gXg03rpG)
[![Websitevideo.gif](https://i.postimg.cc/QxkBP8wd/Websitevideo.gif)](https://postimg.cc/F7zsk4hM)

## Android App Installation Guide

- First start with installing Android Studio on your pc.
- Clone this repository.
- Add the app to a Firebase project on the Firebase console by using the applicationId value specified in the app/build.gradle file of the app as the Android package name.
- Download the generated google-services.json file, and copy it to the app/ directory of the app.
- Enable Phone Authentication, Firestore and Storage on the Firebase console.
- Use the ```gradlew build``` command to build the project directly or use the IDE to run the project to your phone or the emulator.

## Angular App Installation Guide

- First start with installing node and the angular cli on your pc.
- Start the terminal and run commands in the following order.
```
git clone https://github.com/akshaaatt/Swarogya.git
cd angular
npm install
npm start
```
- Now browse to the app at localhost:8000/index.html

## Django App Installation Guide

- Login to test the web app at https://swarogya-web.herokuapp.com/staff_login/ with credentials - Email: dummy@gmail.com , Password: dummy123
- First start with installing python on your pc.
- Then run the command ```$ pip install virtualenv```.
- Clone this repository.
- Update the valid credentials for the files: 
1. firebase-sdk.json
2. Swarogya-36675b238292.json
3. config_file.json
4. firebase-messaging-sw.js
- Start the terminal in django directory and run commands in the following order.
```$ virtualenv env 
$ source env/bin/activate on linux/mac
       OR
$ env/scripts/activate  on windows
(env) $ pip install -r requirements.txt
(env) $ python manage.py migrate
(env) $ python manage.py createsuperuser
(env) $ python manage.py runserver
```
## Cloud Functions Guide

- Install the Firebase CLI to your device.
- Then run the command ```firebase init``` and create a function.
- Replace the code in index.js with the one present in cloud-functions.js
- Run the command ```firebase deploy``` to upload the cloud function to firebase.

## Training the ML models

- Folder jupyter-notebooks contains all the .ipynb notebooks required for training the various ML models.
- These ML models can be saved after training and used later.
- The notebooks are Google Colab compatible and can be run directly via colab.

## Datasets used for Machine Learning Models

The datasets used for training the ML models are taken from https://www.kaggle.com

1. Blood Cell Images: https://www.kaggle.com/paultimothymooney/blood-cells
2. Ocular Disease Recognition: https://www.kaggle.com/andrewmvd/ocular-disease-recognition-odir5k
3. CoronaHack -Chest X-Ray-Dataset: https://www.kaggle.com/praveengovi/coronahack-chest-xraydataset
4. Leukemia Classification: https://www.kaggle.com/andrewmvd/leukemia-classification
5. Chest X-Ray Images (Pneumonia): https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia
6. Head CT - hemorrhage: https://www.kaggle.com/felipekitamura/head-ct-hemorrhage
