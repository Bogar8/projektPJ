package com.feri.projektpj

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyMailboxesActivity : AppCompatActivity(), MailboxAdapter.OnItemClickListener {
    private val TAG: String = this::class.java.simpleName
    val ACTIVITY_ID = 103
    var app: ApplicationMy? = null
    var mailboxAdapter: MailboxAdapter? = null
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_mailboxes)
        app = ApplicationMy()
        recyclerView = findViewById(R.id.rvMailbox)
        initAdapter()
    }

    override fun onItemClick(position: Int) {
        openActivityAddAccess(app?.getMailboxAtPosition(position)?.code)
    }

    override fun onItemLongClick(position: Int) {
        TODO("Not yet implemented")
    }

    fun openActivityAddAccess(code: String?){
        val i = Intent(this@MyMailboxesActivity, AddAccessActivity::class.java)
        i.putExtra(AddAccessActivity().MAILBOX_CODE, code)
        startActivityForResult(i, AddAccessActivity().ACTIVITY_ID)
    }

    fun initAdapter(){
        mailboxAdapter = MailboxAdapter(app,this)
        recyclerView?.setAdapter(mailboxAdapter)
        recyclerView?.setLayoutManager(LinearLayoutManager(this))
    }
}