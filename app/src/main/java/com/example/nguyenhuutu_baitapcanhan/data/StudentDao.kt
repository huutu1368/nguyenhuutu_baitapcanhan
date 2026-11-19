package com.example.nguyenhuutu_baitapcanhan.data

import androidx.room.*
import com.example.nguyenhuutu_baitapcanhan.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StudentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertAll(students: List<Student>)

    @Update
    abstract suspend fun updateStudent(student: Student): Int

    @Delete
    abstract suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students_table WHERE id = :id")
    abstract suspend fun deleteStudentById(id: Int)

    @Query("SELECT * FROM students_table ORDER BY id DESC")
    abstract fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students_table WHERE student_code = :code LIMIT 1")
    abstract suspend fun getStudentByCode(code: String): Student?

    // Mỗi lần tải chỉ thêm mã mới; không ghi đè dữ liệu đã chỉnh sửa ở máy.
    @Transaction
    open suspend fun importNewStudents(students: List<Student>): Int {
        var inserted = 0
        for (student in students) {
            if (getStudentByCode(student.studentCode) == null) {
                insertStudent(student.copy(id = 0))
                inserted++
            }
        }
        return inserted
    }
}
