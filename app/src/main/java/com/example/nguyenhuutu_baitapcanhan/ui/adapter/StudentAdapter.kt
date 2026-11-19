package com.example.nguyenhuutu_baitapcanhan.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nguyenhuutu_baitapcanhan.databinding.ItemStudentRoomBinding
import com.example.nguyenhuutu_baitapcanhan.model.Student
import java.util.Locale

class StudentAdapter(
    private val onStudentClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {
    init { stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StudentViewHolder(
        ItemStudentRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) = holder.bind(getItem(position))
    inner class StudentViewHolder(private val binding: ItemStudentRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) = with(binding) {
            tvFullName.text = student.fullName
            tvStudentCode.text = "MSSV: ${student.studentCode}"
            tvEmail.text = student.email
            tvMajor.text = student.major
            tvGpa.text = String.format(Locale.US, "%.2f", student.gpa)
            root.setOnClickListener { onStudentClick(student) }
            btnEdit.setOnClickListener { onStudentClick(student) }
            btnDelete.setOnClickListener { onDeleteClick(student) }
        }
    }
    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
    }
}
