package com.asai.favorite_phrase.activities

import android.app.Activity
import android.content.Intent
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

    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    private lateinit var binding: ActivityMyAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        // DELETEボタンを押した時、アカウントを削除する
        binding.btnDeleteAccount.setOnClickListener {
            FirestoreClass().deleteUserData()
            FirestoreClass().deleteAccount(this)
            startActivity(Intent(this, IntroActivity::class.java))
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

    // firestoreのユーザー情報から、情報があればそれぞれのテキストを表示
    fun setUserDataInUI(user: User?) {

        mUserDetails = user!!

        binding.etEmail.setText(user.email)
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}