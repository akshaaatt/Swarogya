package com.aemerse.svarogya.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//No serialization of Timestamps yet

@Keep
class User : Parcelable {
    var name: String? = null
    var address: String? = null
    var emailId: String? = null
    var phoneNumber: String? = null
    var dob: String? = null
    var gender: String? = null
    var userId: String? = null
    var firstEmergencyContactNumber: String? = null
    var firstEmergencyContactName: String? = null
    var secondEmergencyContactNumber: String? = null
    var secondEmergencyContactName: String? = null
    var facePic: String? = null
    var bloodGroup: String? = null

    @ServerTimestamp var timestamp: Date? = null
    @ServerTimestamp var joinedSince: Date? = null
    var g: String? = null
    var l: GeoPoint? = null

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        address = parcel.readString()
        emailId = parcel.readString()
        phoneNumber = parcel.readString()
        dob = parcel.readString()
        gender = parcel.readString()
        userId = parcel.readString()
        firstEmergencyContactNumber = parcel.readString()
        firstEmergencyContactName = parcel.readString()
        secondEmergencyContactNumber = parcel.readString()
        secondEmergencyContactName = parcel.readString()
        facePic = parcel.readString()
        bloodGroup = parcel.readString()
        g = parcel.readString()
        val lat = parcel.readDouble()
        val lng = parcel.readDouble()
        l = GeoPoint(lat, lng)
    }

    constructor()
    constructor(name: String?, address:String?, emailId: String?, phoneNumber: String?,
                dob: String?, gender: String?, timestamp: Date?, joinedSince: Date?,
                firstEmergencyContactNumber: String?,firstEmergencyContactName:String?,
                secondEmergencyContactNumber: String?,secondEmergencyContactName:String?,
                userId: String?, bloodGroup:String?,  facePic: String?) {
        this.name = name
        this.firstEmergencyContactNumber = firstEmergencyContactNumber
        this.firstEmergencyContactName = firstEmergencyContactName
        this.secondEmergencyContactNumber = secondEmergencyContactNumber
        this.secondEmergencyContactName = secondEmergencyContactName
        this.address = address
        this.emailId = emailId
        this.dob = dob
        this.phoneNumber = phoneNumber
        this.gender = gender
        this.timestamp = timestamp
        this.joinedSince = joinedSince
        this.userId = userId
        this.bloodGroup = bloodGroup
        this.facePic = facePic
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(emailId)
        parcel.writeString(phoneNumber)
        parcel.writeString(dob)
        parcel.writeString(gender)
        parcel.writeString(userId)
        parcel.writeString(firstEmergencyContactNumber)
        parcel.writeString(firstEmergencyContactName)
        parcel.writeString(secondEmergencyContactNumber)
        parcel.writeString(secondEmergencyContactName)
        parcel.writeString(facePic)
        parcel.writeString(bloodGroup)
        parcel.writeString(g)
        parcel.writeDouble(l!!.latitude)
        parcel.writeDouble(l!!.longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}