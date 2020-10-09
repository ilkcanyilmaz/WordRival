package com.ilkcanyilmaz.wordrival.models

import com.google.gson.annotations.SerializedName

class FCMModel() {
    @SerializedName("to")
    var to: String? = null
    @SerializedName("data")
    var data: Data?=null

    constructor(mTo:String, mData:Data) : this() {
        this.to=mTo
        this.data=mData
    }
    class Data(){
        @SerializedName("title")
        var title:String?=null
        @SerializedName("data")
        var body: String?=null
        constructor(title:String, body:String) : this() {
            this.title=title
            this.body=body
        }
    }
}