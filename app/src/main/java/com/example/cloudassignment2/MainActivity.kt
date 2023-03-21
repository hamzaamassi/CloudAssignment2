package com.example.cloudassignment2


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.cloudassignment2.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var binding: ActivityMainBinding
    private lateinit var listView: ListView
    private lateinit var databaseReference: DatabaseReference
    private var uploads: MutableList<PdfFileInfo> = ArrayList()

    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floatingActionButton = binding.floatBtn

        listView = findViewById(R.id.listview)
        viewAllFiles()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val pdfUpload = uploads[i]

            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = "application/pdf"
            intent.data = Uri.parse(pdfUpload.pdfUrl)
            startActivity(intent)
        }

        floatingActionButton.setOnClickListener {
            val intent = Intent(applicationContext, UploadFileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun viewAllFiles() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploaded")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uploads.clear()
                for (postSnapshot in snapshot.children) {
                    val pdfClass = postSnapshot.getValue(PdfFileInfo::class.java)
                    pdfClass?.let { uploads.add(it) }
                }

                val uploadsArray = uploads.map { it.pdfFileName }.toTypedArray()

                val adapter = object : ArrayAdapter<String>(
                    applicationContext,
                    android.R.layout.simple_list_item_1,
                    uploadsArray
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val text = view.findViewById<TextView>(android.R.id.text1)
                        text.setTextColor(Color.parseColor("#47B9ED"))
                        text.textSize = 24f
                        return view
                    }
                }
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}