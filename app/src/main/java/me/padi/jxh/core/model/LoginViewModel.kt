package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.ApiClient
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.LoginRepository

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val apiClient: ApiClient
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkState<String>>(NetworkState.Idle)

    val loginState = _loginState.asStateFlow()

    fun login(userName: String, password: String) {
        viewModelScope.launch {
            setLoading()
            _loginState.value = loginRepository.login(userName, password)

        }
    }


    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }


    fun setLoading() {
        _loginState.value = NetworkState.Loading
    }


    fun resetState() {
        _loginState.value = NetworkState.Idle
    }

    suspend fun checkLogin(): Boolean {
        return loginRepository.checkLogin()
    }


    private val _hasCheckedInitialLogin = MutableStateFlow(false)
    val hasCheckedInitialLogin = _hasCheckedInitialLogin.asStateFlow()

    suspend fun performInitialLoginCheck(onSuccess: () -> Unit): Boolean {
        if (!_hasCheckedInitialLogin.value) {
            _hasCheckedInitialLogin.value = true
            return checkLogin()
        }
        return false
    }

    fun resetInitialCheck() {
        _hasCheckedInitialLogin.value = false
    }
}