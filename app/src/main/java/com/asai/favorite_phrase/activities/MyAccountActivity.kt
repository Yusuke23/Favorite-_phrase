package com.asai.favorite_phrase.activities

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.asai.favorite_phrase.R
import com.asai.favorite_phrase.databinding.ActivityMyAccountBinding
import com.asai.favorite_phrase.firebase.FirestoreClass
import com.asai.favorite_phrase.models.User
import com.asai.favorite_phrase.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyAccountActivity : BaseActivity() {

    // アップロード用
    private var mSelectedImageFileUri: Uri? =  null
    // ダウンロード用
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    private lateinit var binding: ActivityMyAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        // UPDATEボタンを押した時、画像をアップロードする
        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }
    }

    // アクションバーに戻るボタンをつける
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMyAccountActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_account_title)
        }

        // 前の画面へ戻る
        binding.toolbarMyAccountActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    // デバイスのストレージにアクセス許可を求めた時、デバイスのpermissionの状態がチェックされる
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can also allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // firestoreのユーザー情報から、情報があれば画像とそれぞれのテキストを表示
    fun setUserDataInUI(user: User?) {

        mUserDetails = user!!

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
    }

    // firebase storage に画像を保存
    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null) {

            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(
                        this, mSelectedImageFileUri)
                )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                    e ->
                Toast.makeText(
                    this@MyAccountActivity,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    // 記入、設定された情報をfirestoreに保存
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        FirestoreClass().updateUserAccountData(this, userHashMap)
    }

}