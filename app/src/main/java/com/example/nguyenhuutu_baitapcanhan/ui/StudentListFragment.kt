package com.example.nguyenhuutu_baitapcanhan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nguyenhuutu_baitapcanhan.R
import com.example.nguyenhuutu_baitapcanhan.databinding.FragmentStudentListBinding
import com.example.nguyenhuutu_baitapcanhan.model.Student
import com.example.nguyenhuutu_baitapcanhan.ui.adapter.StudentAdapter
import com.example.nguyenhuutu_baitapcanhan.viewmodel.StudentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class StudentListFragment : Fragment() {
    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels {
        (requireActivity() as MainActivity).studentFactory
    }
    private lateinit var adapter: StudentAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        val left = view.paddingLeft; val top = view.paddingTop
        val right = view.paddingRight; val bottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            root.setPadding(left + bars.left, top + bars.top, right + bars.right,
                bottom + maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(view)
        adapter = StudentAdapter(::showStudentDetailDialog, ::confirmDelete)
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
        binding.edtSearch.setText(viewModel.keyword.value.orEmpty())
        binding.edtSearch.doAfterTextChanged { viewModel.keyword.value = it?.toString().orEmpty() }
        viewModel.visibleStudents.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.layoutEmpty.isVisible = list.isEmpty()
            binding.layoutEmpty.text = if (viewModel.keyword.value.isNullOrBlank())
                "Chưa có sinh viên. Nhấn Thêm để bắt đầu." else "Không tìm thấy sinh viên phù hợp."
            binding.tvStudentCountBadge.text = "DANH BẠ · ${list.size} hồ sơ"
        }
        viewModel.allStudents.observe(viewLifecycleOwner) { list ->
            val average = if (list.isEmpty()) 0.0 else list.map { it.gpa }.average()
            binding.tvTotal.text = list.size.toString()
            binding.tvAverage.text = String.format(Locale.US, "%.2f", average)
        }
        viewModel.statusMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }
        viewModel.syncing.observe(viewLifecycleOwner) { syncing ->
            binding.progress.isVisible = syncing
            binding.toolbar.menu.findItem(R.id.action_sync).isEnabled = !syncing
        }
        binding.fabAdd.setOnClickListener { openForm() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_sync) { viewModel.syncFromCloud(); true } else false
        }
    }
    private fun openForm(student: Student? = null) {
        if (childFragmentManager.findFragmentByTag("STUDENT_FORM") != null) return
        AddStudentDialogFragment.newInstance(student).show(childFragmentManager, "STUDENT_FORM")
    }
    private fun confirmDelete(student: Student) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Xác nhận xóa")
            .setMessage("Xóa sinh viên ${student.fullName} khỏi Room? Không thể hoàn tác.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ -> viewModel.delete(student) }.show()
    }
    private fun showStudentDetailDialog(student: Student) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Thông tin sinh viên")
            .setMessage("ID: ${student.id}\nHọ tên: ${student.fullName}\nMSSV: ${student.studentCode}\nEmail: ${student.email}\nGPA: ${student.gpa}\nNgành: ${student.major}")
            .setPositiveButton("Đóng", null)
            .setNeutralButton("Chỉnh sửa") { _, _ -> openForm(student) }.show()
    }
    override fun onDestroyView() {
        binding.rvStudents.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
