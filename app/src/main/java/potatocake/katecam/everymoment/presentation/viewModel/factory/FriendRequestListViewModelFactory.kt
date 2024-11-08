package potatocake.katecam.everymoment.presentation.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import potatocake.katecam.everymoment.data.repository.FriendRepository
import potatocake.katecam.everymoment.presentation.viewModel.FriendRequestListViewModel

class FriendRequestListViewModelFactory(
    private val friendRepository: FriendRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRequestListViewModel::class.java)) {
            return FriendRequestListViewModel(friendRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}