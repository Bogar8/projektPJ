package com.feri.projektpj

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.data.Mailbox
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class MailboxAdapter  (
    private val sharedApp: ApplicationMy?,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<MailboxAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val view: View = inflater.inflate(R.layout.recycler_view_mailbox_row, parent, false)
        // Return a new holder instance
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //sets content
        val tmp: Mailbox? = sharedApp?.getMailboxAtPosition(position)
        holder.bind(tmp, listener)
    }

    override fun getItemCount(): Int {
        val numberOfWorkDays: Int? = sharedApp?.getMailboxesCount()
        if(numberOfWorkDays != null)
            return numberOfWorkDays
        return 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvMailboxLocation = itemView.findViewById<TextView>(R.id.tvMailboxLocation)
        val tvMailboxStatus = itemView.findViewById<TextView>(R.id.tvMailboxStatus)

        fun bind(workDay: Mailbox?, action: OnItemClickListener){
            tvMailboxLocation.setText(workDay?.location)
            tvMailboxStatus.setText(workDay?.code)

            itemView.setOnClickListener {
                val position: Int = adapterPosition
                if(position != RecyclerView.NO_POSITION)
                    action.onItemClick(position)
            }
            itemView.setOnLongClickListener() {
                action.onItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }
}