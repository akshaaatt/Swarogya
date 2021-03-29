from django.urls import path, re_path
from . import views
from django.conf.urls import url

app_name = 'swarogya_web'
urlpatterns = [
    path('', views.download, name='download'),
    path('hospital_login/', views.hospital_login, name='hospital_login'),
    path('hospital_registration', views.hospital_registration, name='hospital_registration'),
    path('get_user_email/', views.get_user_email, name='get_user_email'),
    path('staff_registration/', views.staff_registration, name='staff_registration'),
    path('staff_login/', views.staff_login, name='staff_login'),
    path('dash/home/', views.dashboard_home, name='dashboard_home'),
    path('dash/addPatient/', views.add_patient_dash, name='add_patient'),
    path('dash/hospitalAnalysis/', views.hospital_analysis_dash, name='hospital_analysis'),
    path('dash/reportAnalysis/', views.report_analysis_dash, name='report_analysis'),
    path('dash/floorAnalysis/', views.floor_analysis_dash, name='floor_analysis'),
    path('dash/wingAnalysis/', views.wing_analysis_dash, name='wing_analysis'),
    path('dash/patientAnalysis/', views.patient_analysis_dash, name='patient_analysis'),
    path('dash/roomAnalysis/', views.room_analysis_dash, name='room_analysis'),
    path('dash/homeCare/', views.home_quarantine_dash, name='home_quarantine'),
    path('dash/notifications/', views.notifications, name='notifications'),
]
