package com.example.everymoment.presentation.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.everymoment.data.repository.FriendRepository
import com.example.everymoment.presentation.viewModel.FriendRequestViewModel

class FriendRequestViewModelFactory(
    private val friendRepository: FriendRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRequestViewModel::class.java)) {
            return FriendRequestViewModel(friendRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}