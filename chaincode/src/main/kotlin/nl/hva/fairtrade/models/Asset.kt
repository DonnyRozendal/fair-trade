package nl.hva.fairtrade.models

import com.google.gson.Gson

abstract class Asset {

    abstract val id: String

    val key: String
        get() = javaClass.simpleName + ":" + id

    val value: String
        get() = Gson().toJson(this)

}