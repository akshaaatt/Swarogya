# Swarogya-GDSC-Solution-Challenge-2021  
# androidapp - https://play.google.com/store/apps/details?id=com.swarogya.app
# webportal - https://www.swarogya.com/
## Django App Installation Guide

- First start with installing python on your pc.
- Then run command ```$ pip install virtualenv```.
- Clone the repository https://github.com/akshaaatt/Swarogya-GDSC-Solution-Challenge-2021
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
## Datasets used for Machine Learning Models

1. Blood Cell Images: https://www.kaggle.com/paultimothymooney/blood-cells
2. Ocular Disease Recognition: https://www.kaggle.com/andrewmvd/ocular-disease-recognition-odir5k
3. CoronaHack -Chest X-Ray-Dataset: https://www.kaggle.com/praveengovi/coronahack-chest-xraydataset
4. Leukemia Classification: https://www.kaggle.com/andrewmvd/leukemia-classification
5. Chest X-Ray Images (Pneumonia): https://www.kaggle.com/paultimothymooney/chest-xray-pneumonia
6. Head CT - hemorrhage: https://www.kaggle.com/felipekitamura/head-ct-hemorrhage
