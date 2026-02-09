package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.ScoreItem
import me.padi.jxh.data.repository.ScoreRepository

class ScoreViewModel(
    private val scoreRepository: ScoreRepository
) : ViewModel() {
    private val _scoreState = MutableStateFlow<NetworkState<List<ScoreItem>>>(NetworkState.Idle)
    val scoreState = _scoreState.asStateFlow()


    fun fetchScore() {
        viewModelScope.launch {
            _scoreState.value = NetworkState.Loading
            _scoreState.value = scoreRepository.fetchScore()
        }
    }



}