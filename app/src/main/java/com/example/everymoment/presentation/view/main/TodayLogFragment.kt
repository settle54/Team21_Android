package com.example.everymoment.presentation.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.everymoment.services.location.LocationService
import com.example.everymoment.R
import com.example.everymoment.data.repository.DiaryRepository
import com.example.everymoment.databinding.FragmentTodayLogBinding
import com.example.everymoment.presentation.adapter.TimelineAdapter
import com.example.everymoment.presentation.view.sub.NotificationFragment
import com.example.everymoment.presentation.viewModel.TimelineViewModel
import com.example.everymoment.presentation.viewModel.factory.TimelineViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.provider.Settings

class TodayLogFragment : Fragment() {

    private val fineLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkCoarseLocationPermission()
            } else {
                showPermissionDeniedDialog("위치 권한")
            }
        }

    private val coarseLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkNotificationPermission()
            } else {
                showPermissionDeniedDialog("위치 권한")
            }
        }

    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationService()
            } else {
                showPermissionDeniedDialog("알림 권한")
            }
        }

    private val backgroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkBackgroundLocation()
            } else {
                showPermissionDeniedDialog("위치 권한")
            }
        }

    private lateinit var binding: FragmentTodayLogBinding
    private lateinit var viewModel: TimelineViewModel
    private val diaryRepository = DiaryRepository()
    private val calendar = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, TimelineViewModelFactory(diaryRepository)).get(
            TimelineViewModel::class.java
        )

        checkFineLocationPermission()

        val adapter = TimelineAdapter(requireActivity(), viewModel)
        setupRecyclerView(adapter)
        observeViewModel(adapter)

        arguments?.getString("selected_date")?.let { selectedDate ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = sdf.parse(selectedDate) ?: calendar.time
            viewModel.fetchDiaries(selectedDate)
        } ?: run {
            val initialDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            viewModel.fetchDiaries(initialDate)
        }

        updateDateText()

        binding.notification.setOnClickListener {
            navigateToNotificationFragment()
        }

        binding.nextDate.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            updateDateText()
            val currentDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            viewModel.fetchDiaries(currentDate)
        }

        binding.prevDate.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            updateDateText()
            val currentDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            viewModel.fetchDiaries(currentDate)
        }
    }

    private fun observeViewModel(adapter: TimelineAdapter) {
        viewModel.diaries.observe(viewLifecycleOwner) { diaryList ->
            adapter.submitList(diaryList)
        }
    }

    private fun updateDateText() {
        val formattedDate = SimpleDateFormat("M월 d일 (E)", Locale("ko", "KR")).format(calendar.time)
        binding.currentDate.text = formattedDate
    }

    private fun navigateToNotificationFragment() {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, NotificationFragment())
            addToBackStack(null)
            commit()
        }
    }

    private fun setupRecyclerView(adapter: TimelineAdapter) {
        binding.timeLineRecyclerView.adapter = adapter
        binding.timeLineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fineLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            checkCoarseLocationPermission()
        }
    }

    private fun checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            coarseLocationPermissionRequest.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            checkBackgroundLocation()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startLocationService()
            }
        } else {
            startLocationService()
        }
    }

    private fun checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                checkNotificationPermission()
            }
        }
    }

    private fun showPermissionDeniedDialog(permissionName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("알림")
            .setMessage("${permissionName}이 허용되어야 앱이 실행됩니다.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("종료") { _, _ ->
                requireActivity().finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun startLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }
}