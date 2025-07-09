package com.example.spawn_app_android.presentation.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class AuthViewModel : ViewModel() {
    var _isLoggedIn = MutableStateFlow(false) //Change this to Override Login

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn

    }

    fun getLoggedIn(): Boolean {
        return _isLoggedIn.value
    }
}
