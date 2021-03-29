# Swarogya-GDSC-Solution-Challenge-2021

## Django App Installation Guide

- First start with installing python on your pc.
- Then run command ```$ pip install virtualenv```.
- Clone the repository https://github.com/akshaaatt/Swarogya-GDSC-Solution-Challenge-2021
- Start the terminal in django directory and run commands in the following order.
```$ virtualenv env 
$ source env/bin/activate on linux/mac
       OR
$ env/scripts/activate  on windows
(env) $ pip install -r requirements.txt
(env) $ python manage.py migrate
(env) $ python manage.py createsuperuser
(env) $ python manage.py runserver```