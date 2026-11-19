package com.example.nguyenhuutu_baitapcanhan.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.*
import com.example.nguyenhuutu_baitapcanhan.model.Student
import com.example.nguyenhuutu_baitapcanhan.repository.StudentRepository
import com.example.nguyenhuutu_baitapcanhan.util.Event
import com.example.nguyenhuutu_baitapcanhan.util.StudentValidator
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SaveResult(val requestId: String, val error: String?)

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _statusMessage = MutableLiveData<Event<String>>()
    val statusMessage: LiveData<Event<String>> = _statusMessage
    val allStudents: LiveData<List<Student>> = repository.allStudents.catch { error ->
        if (error is CancellationException) throw error
        _statusMessage.postValue(Event("Không đọc được Room. Hãy mở lại ứng dụng."))
        emit(emptyList())
    }.asLiveData(Dispatchers.IO)

    val keyword = MutableLiveData("")
    val visibleStudents: LiveData<List<Student>> = MediatorLiveData<List<Student>>().apply {
        fun refresh() {
            val query = normalize(keyword.value.orEmpty().trim())
            value = allStudents.value.orEmpty().filter {
                query.isBlank() || normalize("${it.fullName} ${it.studentCode} ${it.email} ${it.major}").contains(query)
            }
        }
        addSource(allStudents) { refresh() }
        addSource(keyword) { refresh() }
    }
    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving
    private val _syncing = MutableLiveData(false)
    val syncing: LiveData<Boolean> = _syncing
    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    fun insert(student: Student, requestId: String) = save(student, requestId)
    fun update(student: Student, requestId: String) = save(student, requestId)
    private fun save(student: Student, requestId: String) {
        if (_saving.value == true) return
        val validation = listOfNotNull(
            StudentValidator.nameError(student.fullName), StudentValidator.codeError(student.studentCode),
            StudentValidator.emailError(student.email), StudentValidator.gpaError(student.gpa.toString()),
            StudentValidator.majorError(student.major)
        ).firstOrNull()
        if (validation != null) { _saveResult.value = SaveResult(requestId, validation); return }
        _saving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val error = try {
                if (student.id == 0) repository.insert(student) else repository.update(student)
                null
            } catch (error: CancellationException) {
                throw error
            } catch (error: SQLiteConstraintException) {
                "Mã sinh viên đã tồn tại"
            } catch (error: IllegalArgumentException) {
                error.message ?: "Dữ liệu chưa hợp lệ"
            } catch (error: Exception) {
                "Không lưu được sinh viên. Vui lòng thử lại"
            }
            withContext(Dispatchers.Main) {
                _saving.value = false
                _saveResult.value = SaveResult(requestId, error)
                if (error == null) _statusMessage.value = Event("Đã lưu ${student.fullName}")
            }
        }
    }
    fun delete(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.delete(student)
            _statusMessage.postValue(Event("Đã xóa ${student.fullName}"))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _statusMessage.postValue(Event("Không thể xóa sinh viên"))
        }
    }
    fun syncFromCloud() {
        if (_syncing.value == true) return
        _syncing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.syncFromCloud()
            withContext(Dispatchers.Main) {
                _syncing.value = false
                _statusMessage.value = Event(if (result.isSuccess)
                    "Đã thêm ${result.getOrNull()} bản ghi mới từ API; mã đã có được giữ nguyên."
                else "Chưa kết nối được API. Dữ liệu Room vẫn được giữ nguyên.")
            }
        }
    }
    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("""\p{M}+"""), "").replace('đ', 'd').replace('Đ', 'D').lowercase(Locale.ROOT)
}
