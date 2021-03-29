import tensorflow as tf
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image
from django.conf import settings
import numpy as np

session = tf.compat.v1.Session()
graph = tf.compat.v1.get_default_graph()
STATIC_FOLDER = settings.STATIC_ROOT +'/ml_models' # Folder in which models are stored

with session.as_default():
	with graph.as_default():
		model1 = load_model(STATIC_FOLDER + '/' + 'model1.h5')
		model2 = load_model(STATIC_FOLDER + '/' + 'model2.h5')
		model3 = load_model(STATIC_FOLDER + '/' + 'model3.h5')
		model4 = load_model(STATIC_FOLDER + '/' + 'model4.h5')
		model5 = load_model(STATIC_FOLDER + '/' + 'model5.h5')
		model6 = load_model(STATIC_FOLDER + '/' + 'model6.h5')

def BloodCell(full_path):
    data = image.load_img(full_path, target_size=(224, 224, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model1.predict(data)
    indices = {0: 'Eosinophil', 1: 'Lymphocyte', 2: 'Monocyte', 3: 'Neutrophil'}
    predicted_class = np.argmax(result[0])
    accuracy = round(result[0][predicted_class] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy

def Cataract(full_path):
    data = image.load_img(full_path, target_size=(224, 224, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model2.predict(data)
    indices = {0: 'Normal', 1: 'Cataract'}
    predicted_class = np.argmax(result[0])
    accuracy = round(result[0][predicted_class] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy

def Corona(full_path):
    data = image.load_img(full_path, target_size=(224, 224, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model3.predict(data)
    indices = {0: 'Corona', 1: 'Normal'}
    predicted_class = np.argmax(result[0])
    accuracy = round(result[0][predicted_class] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy

def Leukemia(full_path):
    data = image.load_img(full_path, target_size=(128, 128, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model4.predict(data)
    if result[0][0] < 0.5:
    	predicted_class = 0
    else:
    	predicted_class = 1
    indices = {0: 'Positive', 1: 'Negative'}
    accuracy = round(result[0][0] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy

def Pneumonia(full_path):
    data = image.load_img(full_path, target_size=(224, 224, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model5.predict(data)
    indices = {0: 'Normal', 1: 'Pneumonia'}
    predicted_class = np.argmax(result[0])
    accuracy = round(result[0][predicted_class] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy

def HeadCT(full_path):
    data = image.load_img(full_path, target_size=(128, 128, 3))
    data = image.img_to_array(data)
    data = np.expand_dims(data, axis=0)
    with session.as_default():
        with graph.as_default():
        	result = model6.predict(data)
    if result[0][0] < 0.5:
    	predicted_class = 0
    else:
    	predicted_class = 1
    indices = {0: 'Normal', 1: 'Hemmorhage'}
    accuracy = round(result[0][0] * 100, 2)
    label = indices[predicted_class]
    return label, accuracy