package com.example.nguyenhuutu_baitapcanhan.ui

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.example.nguyenhuutu_baitapcanhan.databinding.DialogAddStudentBinding
import com.example.nguyenhuutu_baitapcanhan.model.Student
import com.example.nguyenhuutu_baitapcanhan.util.StudentValidator
import com.example.nguyenhuutu_baitapcanhan.viewmodel.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale
import java.util.UUID

class AddStudentDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogAddStudentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels {
        (requireActivity() as MainActivity).studentFactory
    }
    private var requestId = ""
    private val editingId get() = arguments?.getInt("id", 0) ?: 0
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requestId = state?.getString("request") ?: UUID.randomUUID().toString()
        // Dialog dùng binding cho nội dung sheet và quan sát theo lifecycle của DialogFragment.
        viewModel.saveResult.observe(this) { result ->
            if (result.requestId == requestId) {
                if (result.error == null) dismiss()
                else _binding?.let { ui ->
                    ui.tvError.text = result.error
                    ui.tvError.isVisible = true
                }
            }
        }
        viewModel.saving.observe(this) { busy -> updateSaving(busy) }
    }
    override fun onCreateDialog(state: Bundle?): Dialog {
        _binding = DialogAddStudentBinding.inflate(layoutInflater)
        fun text(key: String, fallback: String = "") = state?.getString(key)
            ?: arguments?.getString(key) ?: fallback
        binding.etFullName.setText(text("name"))
        binding.etStudentCode.setText(text("code"))
        binding.etEmail.setText(text("email"))
        binding.etGpa.setText(text("gpa"))
        binding.etMajor.setText(text("major", "Công Nghệ Thông Tin"))
        binding.etFullName.doAfterTextChanged { binding.tilFullName.error = StudentValidator.nameError(name()) }
        binding.etStudentCode.doAfterTextChanged { binding.tilStudentCode.error = StudentValidator.codeError(code()) }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = StudentValidator.emailError(email()) }
        binding.etGpa.doAfterTextChanged { binding.tilGpa.error = StudentValidator.gpaError(gpa()) }
        binding.etMajor.doAfterTextChanged { binding.tilMajor.error = StudentValidator.majorError(major()) }
        binding.tvFormTitle.text = if (editingId == 0) "Tạo hồ sơ sinh viên" else "Cập nhật hồ sơ"
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }
        return BottomSheetDialog(requireContext()).apply { setContentView(binding.root) }
    }
    override fun onStart() {
        super.onStart()
        val sheetDialog = dialog as BottomSheetDialog
        val sheet = sheetDialog.findViewById<android.view.View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.let {
            it.layoutParams = it.layoutParams.apply { height = ViewGroup.LayoutParams.MATCH_PARENT }
            BottomSheetBehavior.from(it).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
        // Chiều cao nội dung được giới hạn để ScrollView cuộn được khi bàn phím mở.
        val screenHeight = resources.displayMetrics.heightPixels
        binding.root.layoutParams = binding.root.layoutParams.apply { height = (screenHeight * 0.88f).toInt() }
        val originalBottom = binding.root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight,
                originalBottom + maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
        sheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        updateSaving(viewModel.saving.value == true)
    }
    private fun code() = binding.etStudentCode.text.toString().trim().uppercase(Locale.ROOT)
    private fun name() = binding.etFullName.text.toString().trim().replace(Regex("""\s+"""), " ")
    private fun email() = binding.etEmail.text.toString().trim().lowercase(Locale.ROOT)
    private fun gpa() = binding.etGpa.text.toString().trim()
    private fun major() = binding.etMajor.text.toString().trim()
    private fun save() {
        val errors = listOf(StudentValidator.nameError(name()), StudentValidator.codeError(code()),
            StudentValidator.emailError(email()), StudentValidator.gpaError(gpa()), StudentValidator.majorError(major()))
        binding.tilFullName.error = errors[0]; binding.tilStudentCode.error = errors[1]
        binding.tilEmail.error = errors[2]; binding.tilGpa.error = errors[3]; binding.tilMajor.error = errors[4]
        if (errors.any { it != null }) return
        binding.tvError.isVisible = false
        val student = Student(editingId, name(), code(), email(), StudentValidator.parseGpa(gpa())!!, major())
        if (editingId == 0) viewModel.insert(student, requestId) else viewModel.update(student, requestId)
    }
    private fun updateSaving(busy: Boolean) {
        val ui = _binding ?: return
        isCancelable = !busy
        ui.btnSave.isEnabled = !busy
        ui.btnCancel.isEnabled = !busy
        ui.btnSave.text = if (busy) "Đang lưu..." else "Lưu hồ sơ"
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = !busy
        listOf(ui.etFullName, ui.etStudentCode, ui.etEmail, ui.etGpa, ui.etMajor).forEach { it.isEnabled = !busy }
    }
    override fun onSaveInstanceState(out: Bundle) {
        out.putString("request", requestId)
        _binding?.let { ui ->
            out.putString("name", ui.etFullName.text.toString()); out.putString("code", ui.etStudentCode.text.toString())
            out.putString("email", ui.etEmail.text.toString()); out.putString("gpa", ui.etGpa.text.toString())
            out.putString("major", ui.etMajor.text.toString())
        }
        super.onSaveInstanceState(out)
    }
    override fun onDestroyView() { _binding = null; super.onDestroyView() }
    companion object {
        fun newInstance(student: Student? = null) = AddStudentDialogFragment().apply {
            arguments = Bundle().apply {
                student?.let {
                    putInt("id", it.id); putString("name", it.fullName); putString("code", it.studentCode)
                    putString("email", it.email); putString("gpa", it.gpa.toString()); putString("major", it.major)
                }
            }
        }
    }
}
