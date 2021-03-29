const functions = require('firebase-functions');
const admin = require('firebase-admin');
const FieldValue = admin.firestore.FieldValue;
admin.initializeApp();

exports.manageAfterTheTreatmentDone = functions.firestore.document('OngoingTreatments/{patientId}')
    .onDelete((snap) => {
        return admin.firestore().collection("TreatedPatients").doc(snap.id).set(snap.data())
    });

exports.alerts = functions.firestore.document('OngoingTreatments/{patientId}')
    .onUpdate((change) => {
        const newValue = change.after.data();

        const needAssistanceAlert = newValue.needAssistanceAlert;
        const emergencyAlert = newValue.emergencyAlert;
        const vitalParametersAlert = newValue.vitalParametersAlert;
        const hospitalName = newValue.hospitalName;
        const floorNumber = newValue.floorNumber;
        const wingNumber = newValue.wingNumber;

        if(needAssistanceAlert){
            const notificationContent = {
                data: {
                    title: newValue.name + " needs Assistance",
                    body: `Bed Number: ${newValue.bedNumber}<br>Room Number: ${newValue.roomNumber}<br>Wing Number: ${newValue.wingNumber}<br>Floor Number: ${newValue.floorNumber}`
                }
            };

            admin.firestore().collection("OngoingTreatments").doc(change.after.id).update("needAssistanceAlert",FieldValue.delete());

            return sendNotifications(notificationContent)
        }
        if(emergencyAlert){
            const notificationContent = {
                data: {
                    title: newValue.name + " states Emergency",
                    body: `Bed Number: ${newValue.bedNumber}<br>Room Number: ${newValue.roomNumber}<br>Wing Number: ${newValue.wingNumber}<br>Floor Number: ${newValue.floorNumber}`,
                }
            };

            admin.firestore().collection("OngoingTreatments").doc(change.after.id).update("emergencyAlert",FieldValue.delete());

            return sendNotifications(notificationContent)
        }
        if(vitalParametersAlert){
            const notificationContent = {
                data: {
                    title: newValue.name + " has vital parameters out of range",
                    body: `Bed Number: ${newValue.bedNumber}<br>Room Number: ${newValue.roomNumber}<br>Wing Number: ${newValue.wingNumber}<br>Floor Number: ${newValue.floorNumber}`,
                }
            };

            admin.firestore().collection("OngoingTreatments").doc(change.after.id).update("vitalParametersAlert",FieldValue.delete());

            return sendNotifications(notificationContent)
        }
        if(!change.before.data().joinedOn.isEqual(change.after.data().joinedOn)){
            const notificationContent = {
                data: {
                    title: newValue.name + " just Registered!",
                    body: `Bed Number: ${newValue.bedNumber}<br>Room Number: ${newValue.roomNumber}<br>Wing Number: ${newValue.wingNumber}<br>Floor Number: ${newValue.floorNumber}`
                }
            };

            return sendNotifications(notificationContent)
        }
        return null

        function sendNotifications(notificationContent) {
            admin.firestore().collection("Employees").where("hospitalName","==",hospitalName)
                .where("floorNumber","==",floorNumber)
                .where("wingNumber","==",wingNumber)
                .get()
                .then((querySnapshot) =>{
                    return querySnapshot.forEach((doc) => {
                        const token = doc.data().token;

                        if(token!==null) {
                            return admin.messaging().sendToDevice(token, notificationContent);
                        }
                        else{
                            return null
                        }
                    });
                })
                .catch((error) => {
                    console.log("Error getting documents: ", error);
                });
        }
    });
