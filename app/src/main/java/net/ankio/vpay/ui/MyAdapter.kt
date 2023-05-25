package net.ankio.vpay.ui


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ankio.vpay.R
import net.ankio.vpay.utils.PayInfo
import net.ankio.vpay.utils.PushUtils


class MyAdapter(private val dataList: ArrayList<PayInfo>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    private lateinit var mContext: Context
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView
        var money: TextView
        var imageView2: ImageView
        var button: Button
        init {
            time = itemView.findViewById(R.id.time)
            money = itemView.findViewById(R.id.money)
            imageView2 = itemView.findViewById(R.id.imageView2)
            button = itemView.findViewById(R.id.button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_layout, parent, false)
        mContext = parent.context
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.money.text = "金额：￥${data.money}"
        holder.time.text = "时间：￥${data.time}"
        if(data.type==1){
            holder.imageView2.setImageResource(R.drawable.alipay_icon)
        }else{
            holder.imageView2.setImageResource(R.drawable.wechat_icon)
        }
        holder.button.setOnClickListener{
            Toast.makeText(mContext,"已在后台回调，可查看日志！",Toast.LENGTH_LONG).show()
            Thread {
                PushUtils.appPush(data.type,data.money,mContext)
            }.start()
        }

    }



    override fun getItemCount(): Int {
        return dataList.size
    }
}
