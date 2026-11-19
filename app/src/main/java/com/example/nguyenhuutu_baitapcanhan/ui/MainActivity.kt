package com.example.nguyenhuutu_baitapcanhan.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.nguyenhuutu_baitapcanhan.R
import com.example.nguyenhuutu_baitapcanhan.data.AppDatabase
import com.example.nguyenhuutu_baitapcanhan.repository.StudentRepository
import com.example.nguyenhuutu_baitapcanhan.viewmodel.StudentViewModel
import com.example.nguyenhuutu_baitapcanhan.viewmodel.StudentViewModelFactory

class MainActivity : AppCompatActivity() {
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { StudentRepository(database.studentDao()) }
    val studentFactory by lazy { StudentViewModelFactory(repository) }
    private val viewModel: StudentViewModel by viewModels { studentFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        // Khởi tạo ViewModel chung trước khi Fragment truy cập.
        viewModel.allStudents
        if (supportFragmentManager.findFragmentById(R.id.fragment_container_view) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, StudentListFragment()).commit()
        }
    }
}
