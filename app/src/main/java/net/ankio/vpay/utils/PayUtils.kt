package net.ankio.vpay.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object PayUtils {
    fun add(time: String, type: Int, money: Float){
        this.removedLast100()
        val list = list()
        val item = PayInfo(time,type,money)
        list.add(item)
        SpUtils.putString("pay", Gson().toJson(list))
    }

    fun list(): ArrayList<PayInfo> {
        var list = ArrayList<PayInfo>()
        val string = SpUtils.getString("pay")
        if(string!==""){
            val gson = Gson()
            list =
                gson.fromJson(string, object : TypeToken<ArrayList<PayInfo>?>() {}.type)
        }
        return list;
    }

    private fun removedLast100(){
       val list = list()
        if(list.size>200){
            val new_list = ArrayList<PayInfo>()
            var count = 200
            list.forEach {
                if(count<=0){
                    new_list.add(it)
                }
                count--
            }
            SpUtils.putString("pay", Gson().toJson(new_list))
        }
    }
}