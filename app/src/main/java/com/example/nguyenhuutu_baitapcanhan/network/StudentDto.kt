package com.example.nguyenhuutu_baitapcanhan.network

import com.example.nguyenhuutu_baitapcanhan.model.Student
import java.util.Locale

// JSONPlaceholder /users trả về id, name, email; không có mã SV, GPA hay ngành.
data class StudentDto(val id: Int, val name: String, val email: String)

fun StudentDto.toEntity(): Student = Student(
    id = 0, // ID Room tự sinh, không lấy ID API ghi đè bản ghi cục bộ.
    fullName = name.trim(),
    studentCode = "API" + id.toString().padStart(4, '0'),
    email = email.trim().lowercase(Locale.ROOT),
    gpa = 0.0, // Giá trị demo do API không có GPA, KHÔNG phải điểm thực tế.
    major = "Dữ liệu API mẫu"
)
