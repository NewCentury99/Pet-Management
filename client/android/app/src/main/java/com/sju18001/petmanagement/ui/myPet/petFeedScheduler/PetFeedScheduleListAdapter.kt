package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R

class PetFeedScheduleListAdapter(private val dataSet: ArrayList<PetFeedScheduleListItem>, private val petNameForId: HashMap<Long, String>) : RecyclerView.Adapter<PetFeedScheduleListAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val noonTextView: TextView = view.findViewById(R.id.noon_text_view)
        val feedTimeTextView: TextView = view.findViewById(R.id.feed_time_text_view)
        val isTurnedOnSwitch: Switch = view.findViewById(R.id.is_turned_on_switch)
        val petListTextView: TextView = view.findViewById(R.id.pet_list_text_view)
        val memoTextView: TextView = view.findViewById(R.id.memo_text_view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_feed_schedule_list_item, parent, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])

        // 스위치
        holder.isTurnedOnSwitch.setOnClickListener {
            dataSet[position].isTurnedOn = !dataSet[position].isTurnedOn
        }
    }

    override fun getItemCount(): Int = dataSet.size

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: ViewHolder, data: PetFeedScheduleListItem){
        holder.noonTextView.text = if(data.feedTime!!.hour <= 12) "오전" else "오후"
        holder.feedTimeTextView.text = data.feedTime!!.hour.toString().padStart(2, '0') + ":" + data.feedTime!!.minute.toString().padStart(2, '0')
        holder.isTurnedOnSwitch.isChecked = data.isTurnedOn!!
        holder.petListTextView.text = setPetListString(data.petList)
        holder.memoTextView.text = data.memo
    }

    private fun setPetListString(petList: ArrayList<Long>): String{
        var result = ""
        val size = petList.size

        for(i:Int in 0 until size-1){
            val id = petList[i]
            result += if(petNameForId[id] != null) petNameForId[id] else id.toString()
            result += ", "
        }
        result += if(petNameForId[petList.last()] != null) petNameForId[petList.last()] else petList.last().toString()

        return result
    }

    fun addItem(item: PetFeedScheduleListItem){
        dataSet.add(item)
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
    }
}