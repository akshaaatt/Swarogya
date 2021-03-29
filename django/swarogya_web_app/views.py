from django.http import HttpResponse,HttpResponseRedirect
from django.shortcuts import get_object_or_404, render, redirect
from django.urls import reverse
from django.http import JsonResponse
import json
import requests
from . import utils
from django.core.files.storage import FileSystemStorage
from getpass import getpass
from django.conf import settings
from .models import Employee,SmartPatient,ManualPatient,floor_analysis_db,room_analysis_db,hospital_analysis_db,get_pic_url,check_if_patientId_present,UpdatePatient,get_patient_report,home_quarantine_db
from .models import treated_patient,wing_analysis_db,analysis_func, get_context,removeEmployee,user_auth,get_searchPatient,get_hospital_name,update_patient_details,QuarantinePatient,get_employees

def index(request):
    return render(request,'verify_admin.html')

def hospital_registration(request):
    return render(request,'hospital_registration.html')

def hospital_verification(request):
    return render(request,'verify_admin.html')

def hospital_login(request):
    return render(request,'hospital_login.html')

def notifications(request):
    return render(request,'dashboard_final/notifications.html')

def staff_registration(request):
    if request.method == 'POST':
        if request.POST.get('registration') != None:
            try:
                user_auth(request.POST.get('staff_email'), request.POST.get('psw'))
                staff_to_register = Employee(request.POST.get('staff_name'),request.POST.get('staff_id'),
                request.POST.get('phone'),request.POST.get('staff_email'),
                request.POST.get('staff_designation'), request.session["hospitalName"] )
                staff_to_register.add_doc_firebase()
                return render(request,'staff_registration.html', {"validity":'Registered'})
            except requests.exceptions.HTTPError as err:
                return render(request,'staff_registration.html', {"validity":"Registration Failed, User Email Already Exists"})
        else:
            return render(request,'staff_registration.html', {"validity": removeEmployee(request.POST.get('toRemoveEmail'))})
    return render(request,'staff_registration.html', {})

def staff_login(request):
    return render(request,'staff_login.html')

def download(request):
    response = redirect('/staff_login/')
    return response

def trial(request):
    return render(request,'trial.html')

def get_user_email(request):
    login_user = get_hospital_name(request.GET.get('user_email'))
    request.session["hospitalName"] = login_user['hospitalName']
    request.session["user_email"] = request.GET.get('user_email')
    request.session["user_uid"] = request.GET.get('user_uid')

    data = {
    'hospitalName' : request.session["hospitalName"],
    'admin': login_user['admin'],
    'doc_id': login_user['doc_id'],
    }
    return JsonResponse(data)

def dashboard_home(request):
    return render(request,'dashboard_final/dash_home.html',{'hospitalName':request.session["hospitalName"]})

def patient_analysis_dash(request):
    if request.method == 'POST':
        if(request.POST.get('search') != None):
            data = get_searchPatient(request.POST.get('searchPatient'),request.session['hospitalName'])
            if( data == "No Registered Patient with the Hospital"):
                return render(request,'dashboard_final/patient_analysis.html', {'to_alert': data})
            else:
                request.session['patient_doc_id'] = data[1]
                request.session['patient_data'] = data[0]
                return render(request,'dashboard_final/patient_analysis.html', data[0])
        elif(request.POST.get('updatePatient') != None):
            patient = UpdatePatient(request.POST.get('name'),request.POST.get('address'),request.POST.get('phone_no'),request.POST.get('gender'),
            request.POST.get('dob'),request.POST.get('bloodGroup'),request.POST.get('email_id'),request.POST.get('first_emergency_contact'),
            request.POST.get('first_emergency_contact_number'),request.POST.get('second_emergency_contact'),
            request.POST.get('second_emergency_contact_number'),request.POST.get('barcodeid'),request.POST.get('hospital_name'),
            request.POST.get('wing_no'),request.POST.get('floor_no'),request.POST.get('room_no'),request.POST.get('bed_no'))
            update_patient_details(request.session['patient_doc_id'],patient)
            return render(request,'dashboard_final/patient_analysis.html', {'to_alert': "Patient Updated"})
        elif(request.POST.get('generate_report')!=None):
            get_patient_report(request.session['patient_data'])
            request.session['patient_data'].update({'context':"Patient Report Generated"})
            return render(request,'dashboard_final/patient_analysis.html', request.session['patient_data'])
    return render(request,'dashboard_final/patient_analysis.html')

def upload_pics(request,patientId):
    try:
        dict = {}
        added_files = []
        for file in request.FILES.getlist('uploaded_files'):
            if file.name:
                name = file.name.split("_")
                if not (name[0] in added_files):
                    dict.update({name[0] : {}})
                    added_files.append(name[0])
                number = name[1]
                number_image = number[0].split('.')
                url = get_pic_url(file.chunks(),name[0],number, patientId, request.session['user_uid'])
                dict[name[0]][number_image[0]] = url
                return dict
    except MultiValueDictKeyError as err:
        return None

def add_patient_dash(request):
    context = {}
    context['hospitalName'] = request.session['hospitalName']
    if request.method == 'POST':
        if(request.POST.get('smartPatient') != None):
            if check_if_patientId_present(request.POST.get('barcodeid1')) == "ID not Present":
                patient = SmartPatient(request.POST.get('barcodeid1'),request.session["hospitalName"],
                request.POST.get('wing_no'),request.POST.get('floor_no'),request.POST.get('room_no'),request.POST.get('bed_no'),upload_pics(request,request.POST.get('barcodeid1')))
                context['to_alert'] = patient.add_patient_firebase()
            else:
                context['to_alert'] = "Patient ID Present!! Use another Patient ID"
        elif(request.POST.get('manualPatient') != None):
            if check_if_patientId_present(request.POST.get('barcodeid2')) == "ID not Present":
                facePic = request.FILES['face_pic'].name.split("_")
                facePic_url = get_pic_url(request.FILES['face_pic'].chunks(), facePic[0],facePic[1], request.POST.get('barcodeid2'), request.session['user_uid'])
                patient = ManualPatient(request.POST.get('name'),request.POST.get('address'),request.POST.get('phone_no'),request.POST.get('gender'),
                request.POST.get('dob'),request.POST.get('bloodGroup'),request.POST.get('email_id'),request.POST.get('first_emergency_contact'),
                request.POST.get('first_emergency_contact_number'),request.POST.get('second_emergency_contact'),
                request.POST.get('second_emergency_contact_number'),request.POST.get('barcodeid2'),request.session["hospitalName"],
                request.POST.get('wing_no'),request.POST.get('floor_no'),request.POST.get('room_no'),request.POST.get('bed_no'),request.POST.get('treatmentMode'),facePic_url,upload_pics(request,request.POST.get('barcodeid2')))
                context['to_alert'] = patient.add_patient_firebase()
            else:
                context['to_alert'] = "Patient ID Present!! Use another Patient ID"
        elif(request.POST.get('home_quarantine')!= None):
            if check_if_patientId_present(request.POST.get('barcodeid4')) == "ID not Present":
                patient = QuarantinePatient(request.POST.get('barcodeid4'),request.session["hospitalName"],upload_pics(request,request.POST.get('barcodeid4')))
                context['to_alert'] = patient.add_patient_firebase()
            else:
                context['to_alert'] = "Patient ID Present!! Use another Patient ID"
        else:
            context['to_alert'] = treated_patient(request.POST.get('barcodeid3'),request.POST.get('status'))

    return render(request,'dashboard_final/add_patient.html', context)

content = {}

def report_analysis_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        content['submitted']=True
        fs = FileSystemStorage()

        myfile = request.FILES.get('headct')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['headct_result']=utils.HeadCT(uploaded_file_url)

        myfile = request.FILES.get('pneumonia')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['pneumonia_result']=utils.Pneumonia(uploaded_file_url)

        myfile = request.FILES.get('leukemia')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['leukemia_result']=utils.Leukemia(uploaded_file_url)
        
        myfile = request.FILES.get('corona')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['corona_result']=utils.Corona(uploaded_file_url)
        
        myfile = request.FILES.get('bloodcell')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['bloodcell_result']=utils.BloodCell(uploaded_file_url)
        
        myfile = request.FILES.get('cataract')
        if(myfile):
            filename = fs.save(myfile.name, myfile)
            uploaded_file_url = settings.BASE_DIR + fs.url(filename)
            content['cataract_result']=utils.Cataract(uploaded_file_url)

        return render(request,'dashboard_final/report_analysis.html',content)
    return render(request,'dashboard_final/report_analysis.html',content)

def hospital_analysis_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        db = hospital_analysis_db(request.session["hospitalName"])
        return render(request,'dashboard_final/hospital_analysis.html', get_context(db['doc'],request.POST.get('date'),request.POST.get('week'),request.POST.get('month'),db['type_analysis'],content['hospitalName']))
    return render(request,'dashboard_final/hospital_analysis.html',content)

def floor_analysis_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        db = floor_analysis_db(request.session["hospitalName"], request.POST.get('wing_no'),request.POST.get('floor_no'))
        return render(request,'dashboard_final/floor_analysis.html', get_context(db['doc'],request.POST.get('date'),request.POST.get('week'),request.POST.get('month'),db['type_analysis'],content['hospitalName']))
    return render(request,'dashboard_final/floor_analysis.html',content)

def wing_analysis_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        db = wing_analysis_db(request.session["hospitalName"], request.POST.get('wing_no'))
        return render(request,'dashboard_final/wing_analysis.html', get_context(db['doc'],request.POST.get('date'),request.POST.get('week'),request.POST.get('month'),db['type_analysis'],content['hospitalName']))
    return render(request,'dashboard_final/wing_analysis.html',content)

def room_analysis_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        db = room_analysis_db(request.session["hospitalName"], request.POST.get('wing_no'), request.POST.get('floor_no'), request.POST.get('room_no'))
        return render(request,'dashboard_final/room_analysis.html',get_context(db['doc'],request.POST.get('date'),request.POST.get('week'),request.POST.get('month'),db['type_analysis'],content['hospitalName']))
    return render(request,'dashboard_final/room_analysis.html',content)

def home_quarantine_dash(request):
    content['hospitalName'] = request.session["hospitalName"]
    if request.method == 'POST':
        db = home_quarantine_db(request.session["hospitalName"])
        return render(request,'dashboard_final/home_quarantine.html', get_context(db['doc'],request.POST.get('date'),request.POST.get('week'),request.POST.get('month'),db['type_analysis'],content['hospitalName']))
    return render(request,'dashboard_final/home_quarantine.html',content)
