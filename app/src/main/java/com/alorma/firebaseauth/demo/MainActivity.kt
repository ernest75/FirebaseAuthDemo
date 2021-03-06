package com.alorma.firebaseauth.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alorma.firebaseauth.demo.ui.MainViewModel
import com.alorma.firebaseauth.demo.ui.adapter.ForlayoAdapter
import com.alorma.firebaseauth.demo.ui.model.ForlayoVM
import com.alorma.firebaseauth.demo.ui.model.UserVM
import com.alorma.firebaseauth.demo.ui.uiModule
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_user_layout.*
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.context.loadKoinModules

class MainActivity : AppCompatActivity() {

    private val adapter: ForlayoAdapter by lazy { ForlayoAdapter() }

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadKoinModules(uiModule)

        mainViewModel.user.observe(this, Observer {
            it?.let { user -> onUser(user) }
        })

        mainViewModel.forlayos.observe(this, Observer {
            it?.let { forlayos -> onForlayos(forlayos) }
        })

        mainViewModel.loadUser()

        floating.setOnClickListener {
            mainViewModel.createForlayo()
        }

        forlayosList.adapter = adapter
        forlayosList.layoutManager = LinearLayoutManager(this)
    }

    private fun onUser(user: UserVM) {
        when (user) {
            UserVM.NoUser -> {
                userInfo.visibility = View.GONE
                signInButton.visibility = View.VISIBLE
                signInButton.setOnClickListener {
                    onSignIn()
                }
                signOutButton.visibility = View.GONE
                signOutButton.setOnClickListener(null)
                deleteButton.visibility = View.GONE
                deleteButton.setOnClickListener(null)
            }
            is UserVM.LoggedUser -> {
                userInfo.visibility = View.VISIBLE
                signInButton.visibility = View.GONE
                signInButton.setOnClickListener(null)
                userName.text = user.userName
                userEmail.text = user.email
                userPhone.text = user.phone
                userAvatar?.let {
                    Glide.with(userAvatar).load(user.avatar).into(userAvatar)
                }
                signOutButton.visibility = View.VISIBLE
                signOutButton.setOnClickListener {
                    onSignOut()
                }
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    onDelete()
                }
            }
        }
    }

    private fun onForlayos(forlayos: List<ForlayoVM>) {
        adapter.submitList(forlayos)
    }

    private fun onSignIn() {
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.AnonymousBuilder().build(),
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.GitHubBuilder().build()
                )
            )
            .setLogo(R.mipmap.ic_launcher_round)
            .build()

        startActivityForResult(intent, REQUEST_LOGIN)
    }

    private fun onSignOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            mainViewModel.loadUser()
        }
    }

    private fun onDelete() {
        AuthUI.getInstance().delete(this).addOnSuccessListener {
            mainViewModel.loadUser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        mainViewModel.loadUser()
    }

    companion object {
        const val REQUEST_LOGIN = 112
    }
}
