package com.example.inventory.utils
import android.os.Parcel
import android.os.Parcelable
data class ProductData(
                        var productName:String? = null,
                       var productDescription:String? = null,
                       var costPrice:String? = null,
                       var sellingPrice:String? = null,
                       var quantity:String? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productName)
        parcel.writeString(productDescription)
        parcel.writeString(costPrice)
        parcel.writeString(sellingPrice)
        parcel.writeString(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductData> {
        override fun createFromParcel(parcel: Parcel): ProductData {
            return ProductData(parcel)
        }

        override fun newArray(size: Int): Array<ProductData?> {
            return arrayOfNulls(size)
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "productName" to productName,
            "productDescription" to productDescription,
            "costPrice" to costPrice,
            "sellingPrice" to sellingPrice,
            "quantity" to quantity
            // Add more properties if needed
        )
    }
}

data class ProductUtil(
    var productId:String? = null,
    var productName:String? = null,
    var productDescription:String? = null,
    var costPrice:String? = null,
    var sellingPrice:String? = null,
    var quantity:String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeString(productDescription)
        parcel.writeString(costPrice)
        parcel.writeString(sellingPrice)
        parcel.writeString(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductUtil> {
        override fun createFromParcel(parcel: Parcel): ProductUtil {
            return ProductUtil(parcel)
        }

        override fun newArray(size: Int): Array<ProductUtil?> {
            return arrayOfNulls(size)
        }
    }

//    fun toMap(): Map<String, Any?> {
//        return mapOf(
//            "productName" to productName,
//            "productDescription" to productDescription,
//            "costPrice" to costPrice,
//            "sellingPrice" to sellingPrice,
//            "quantity" to quantity
//            // Add more properties if needed
//        )
//    }
}