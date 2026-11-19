package com.example.nguyenhuutu_baitapcanhan.repository

import com.example.nguyenhuutu_baitapcanhan.data.StudentDao
import com.example.nguyenhuutu_baitapcanhan.model.Student
import com.example.nguyenhuutu_baitapcanhan.network.RetrofitClient
import com.example.nguyenhuutu_baitapcanhan.network.StudentApiService
import com.example.nguyenhuutu_baitapcanhan.network.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StudentRepository(
    private val studentDao: StudentDao,
    private val apiService: StudentApiService = RetrofitClient.apiService
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    private val writeMutex = Mutex()

    suspend fun insert(student: Student): Long = writeMutex.withLock {
        require(studentDao.getStudentByCode(student.studentCode) == null) { "Mã sinh viên đã tồn tại" }
        studentDao.insertStudent(student)
    }
    suspend fun update(student: Student) = writeMutex.withLock {
        val sameCode = studentDao.getStudentByCode(student.studentCode)
        require(sameCode == null || sameCode.id == student.id) { "Mã sinh viên đã tồn tại" }
        check(studentDao.updateStudent(student) == 1) { "Sinh viên không còn tồn tại" }
    }
    suspend fun delete(student: Student) = writeMutex.withLock { studentDao.deleteStudent(student) }

    suspend fun syncFromCloud(): Result<Int> = try {
        val entities = apiService.getStudents().map { it.toEntity() }
        val count = writeMutex.withLock { studentDao.importNewStudents(entities) }
        Result.success(count)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
