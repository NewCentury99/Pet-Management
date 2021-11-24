package com.sju18001.petmanagement.ui.myPet.petManager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Pet
import com.sju18001.petmanagement.restapi.dto.FetchPetPhotoReqDto
import com.sju18001.petmanagement.ui.myPet.MyPetActivity
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

class PetListAdapter(private val startDragListener: OnStartDragListener, private val context: Context) :
    RecyclerView.Adapter<PetListAdapter.HistoryListViewHolder>(), PetListDragAdapter.Listener {

    private var resultList = emptyList<Pet>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val petPhoto: ImageView = itemView.findViewById(R.id.pet_photo)
        val representativePetIcon: ImageView = itemView.findViewById(R.id.representative_pet_icon)
        val petName: TextView = itemView.findViewById(R.id.pet_name)
        val petMessage: TextView = itemView.findViewById(R.id.pet_message)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pet_list_item, parent, false)

        return HistoryListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HistoryListViewHolder, position: Int) {
        val currentItem = resultList[position]

        // check if representative pet
        val isRepresentativePet = currentItem.id == SessionManager.fetchLoggedInAccount(context)?.representativePetId?: 0
        holder.representativePetIcon.visibility = if (isRepresentativePet) View.VISIBLE else View.INVISIBLE

        // set values to views
        if(currentItem.photoUrl != null) {
            fetchPetPhoto(currentItem.id, holder.petPhoto)
        }
        else {
            holder.petPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_pets_60_with_padding))
        }

        // Set name, message
        holder.petName.text = currentItem.name
        holder.petMessage.text = if(currentItem.message.isNullOrEmpty()) context.getString(R.string.filled_heart) else currentItem.message

        // handle button for dragging
        holder.dragHandle.setOnLongClickListener(View.OnLongClickListener {
            this.startDragListener.onStartDrag(holder)
            return@OnLongClickListener false
        })

        // click -> open pet profile
        holder.itemView.setOnClickListener {
            // if pet photo not yet fetched
            if (currentItem.photoUrl != null && holder.petPhoto.drawable == null) {
                return@setOnClickListener
            }

            // set pet values to Intent
            val petProfileIntent = Intent(holder.itemView.context, MyPetActivity::class.java)
            if(currentItem.photoUrl != null) {
                val bitmap = (holder.petPhoto.drawable as BitmapDrawable).bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val photoByteArray = stream.toByteArray()
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_my_pet_selected_pet_photo), photoByteArray)
            }
            else {
                Util.saveByteArrayToSharedPreferences(context, context.getString(R.string.pref_name_byte_arrays),
                    context.getString(R.string.data_name_my_pet_selected_pet_photo), null)
            }
            petProfileIntent.putExtra("petId", currentItem.id)
            petProfileIntent.putExtra("petName", currentItem.name)
            petProfileIntent.putExtra("petBirth",
                if(currentItem.yearOnly!!) currentItem.birth!!.substring(0, 4)
                else currentItem.birth
            )
            petProfileIntent.putExtra("petSpecies", currentItem.species)
            petProfileIntent.putExtra("petBreed", currentItem.breed)
            val petGender = if(currentItem.gender) {
                holder.itemView.context.getString(R.string.pet_gender_female_symbol)
            }
            else {
                holder.itemView.context.getString(R.string.pet_gender_male_symbol)
            }
            val petAge = Period.between(LocalDate.parse(currentItem.birth), LocalDate.now()).years.toString()
            petProfileIntent.putExtra("petGender", petGender)
            petProfileIntent.putExtra("petAge", petAge)
            petProfileIntent.putExtra("petMessage", currentItem.message)
            petProfileIntent.putExtra("isRepresentativePet", isRepresentativePet)

            // open activity
            petProfileIntent.putExtra("fragmentType", "pet_profile_pet_manager")
            holder.itemView.context.startActivity(petProfileIntent)
            (context as Activity).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun getItemCount() = resultList.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(resultList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(resultList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)

        // save order to device
        val petListIdOrder: MutableList<Long> = mutableListOf()
        resultList.map {
            petListIdOrder.add(it.id)
        }
        PetManagerFragment().savePetListOrder(context.getString(R.string.data_name_pet_list_id_order),
            petListIdOrder, context)
    }
    override fun onRowSelected(itemViewHolder: HistoryListViewHolder) {}
    override fun onRowClear(itemViewHolder: HistoryListViewHolder) {}

    // fetch pet photo
    private fun fetchPetPhoto(id: Long, view: View) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .fetchPetPhotoReq(FetchPetPhotoReqDto(id))
        ServerUtil.enqueueApiCall(call, {false}, context, { response ->
            // set fetched photo to view
            (view as ImageView).setImageBitmap(BitmapFactory.decodeStream(response.body()!!.byteStream()))
        }, {}, {})
    }

    public fun setResult(result: List<Pet>){
        this.resultList = result
        notifyDataSetChanged()
    }
}