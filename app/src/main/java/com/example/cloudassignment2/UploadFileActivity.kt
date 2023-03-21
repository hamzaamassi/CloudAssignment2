package com.example.cloudassignment2

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Suppress("DEPRECATION")
class UploadFileActivity : AppCompatActivity() {
    private val REQUEST_CODE_SELECT_PDF = 1
    private lateinit var uploadBtn: Button
    private lateinit var pdfName: EditText

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_file)
        uploadBtn = findViewById(R.id.upload_btn)
        pdfName = findViewById(R.id.name)

        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploaded")

        uploadBtn.setOnClickListener {
            selectPdfFile()
        }
    }

    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Select PDF File"),
            REQUEST_CODE_SELECT_PDF
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PDF && resultCode == RESULT_OK && data != null) {
            uploadPdfFile(data.data)
        }
    }

    private fun uploadPdfFile(pdfUri: Uri?) {
        if (pdfUri == null) {
            Toast.makeText(this, "Please select a PDF file to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfNameStr = pdfName.text.toString().trim()
        if (TextUtils.isEmpty(pdfNameStr)) {
            pdfName.error = "PDF Name is required"
            pdfName.requestFocus()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val pdfRef = storageReference.child("Uploaded/" + System.currentTimeMillis() + ".pdf")

        val uploadTask = pdfRef.putFile(pdfUri)
        uploadTask.addOnCompleteListener(this) {
            progressDialog.dismiss()
        }
        uploadTask.addOnSuccessListener {
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                val pdf = PdfFileInfo(pdfNameStr, uri.toString())
                val uploadId = databaseReference.push().key
                databaseReference.child(uploadId!!).setValue(pdf).addOnSuccessListener {
                    pdfName.setText("")
                    pdfName.clearFocus()
                    Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error uploading file: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded: " + progress.toInt() + "%")
        }
    }
}