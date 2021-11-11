package com.gojol.notto.ui.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gojol.notto.model.data.RepeatType
import com.gojol.notto.model.database.label.Label
import com.gojol.notto.model.database.todo.Todo
import com.gojol.notto.model.datasource.todo.TodoLabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TodoEditViewModel @Inject constructor(private val repository: TodoLabelRepository) :
    ViewModel() {

//    private val fakeRepository = FakeTodoLabelRepository()

    private val _isTodoEditing = MutableLiveData<Boolean>()
    val isTodoEditing: LiveData<Boolean> = _isTodoEditing

    private val _existedTodo = MutableLiveData<Todo>()
    val existedTodo: LiveData<Todo> = _existedTodo

    private val _labelList = MutableLiveData<List<Label>>()
    val labelList: LiveData<List<Label>> = _labelList

    private val _selectedLabelList = MutableLiveData(listOf<Label>())
    val selectedLabelList: LiveData<List<Label>> = _selectedLabelList

    private val _todoContent = MutableLiveData("")
    val todoContent: LiveData<String> = _todoContent

    private val _isRepeatChecked = MutableLiveData(false)
    val isRepeatChecked: LiveData<Boolean> = _isRepeatChecked

    private val _repeatType = MutableLiveData(RepeatType.YEAR)
    val repeatType: LiveData<RepeatType> = _repeatType

    private val _repeatStart = MutableLiveData<String>()
    val repeatStart: LiveData<String> = _repeatStart

    private val _isTimeChecked = MutableLiveData(false)
    val isTimeChecked: LiveData<Boolean> = _isTimeChecked

    private val _timeStart = MutableLiveData<String>()
    val timeStart: LiveData<String> = _timeStart

    private val _timeFinish = MutableLiveData<String>()
    val timeFinish: LiveData<String> = _timeFinish

    private val _timeRepeat = MutableLiveData("5")
    val timeRepeat: LiveData<String> = _timeRepeat

    private val _isKeywordChecked = MutableLiveData(false)
    val isKeywordChecked: LiveData<Boolean> = _isKeywordChecked

    private val _isSaveButtonEnabled = MutableLiveData<Boolean>()
    val isSaveButtonEnabled: LiveData<Boolean> = _isSaveButtonEnabled

    init {
        val date = Date(Calendar.getInstance().timeInMillis)
        _repeatStart.value = getFormattedCurrentDate(date)
        _timeStart.value = getFormattedCurrentTime(date)
        _timeFinish.value = getFormattedCurrentTime(date)
    }

    fun setDummyLabelData() {
        viewModelScope.launch {
            _labelList.value = repository.getAllLabel()
        }
    }

    fun addLabelToSelectedLabelList(labelName: String) {
        if (selectedLabelList.value?.find { it.name == labelName } != null) return

        val label = labelList.value?.find { it.name == labelName } ?: return
        val newLabelList = selectedLabelList.value?.toMutableList()?.apply {
            add(label)
        } ?: return
        _selectedLabelList.value = newLabelList
    }

    fun removeLabelFromSelectedLabelList(label: Label) {
        val newLabelList = selectedLabelList.value?.toMutableList()?.apply {
            remove(label)
        } ?: return
        _selectedLabelList.value = newLabelList
    }

    fun updateTodoContent(content: String) {
        _todoContent.value = content
    }

    fun updateIsRepeatChecked(isChecked: Boolean) {
        _isRepeatChecked.value = isChecked
    }

    fun updateIsTimeChecked(isChecked: Boolean) {
        _isTimeChecked.value = isChecked
    }

    fun updateIsKeywordChecked(isChecked: Boolean) {
        _isKeywordChecked.value = isChecked
    }

    fun updateIsSaveButtonEnabled(bool: Boolean) {
        _isSaveButtonEnabled.value = bool
    }

    fun saveTodo() {
        saveNewTodo()
    }

    private fun saveNewTodo() {
        if (todoContent.value.isNullOrEmpty()) {
            _isSaveButtonEnabled.value = false
            return
        }

        val newTodo = Todo(
            todoContent.value!!,
            isRepeatChecked.value ?: return,
            repeatType.value ?: return,
            repeatStart.value ?: return,
            isTimeChecked.value ?: return,
            timeStart.value ?: return,
            timeFinish.value ?: return,
            timeRepeat.value ?: return,
            isKeywordChecked.value ?: return,
            false,
            ""
        )

        viewModelScope.launch {
            val job = launch { repository.insertTodo(newTodo) }.join()

            val saveTodo = withContext(Dispatchers.Default) {
                repository.getTodosWithLabels().find { it.labels.isEmpty() }?.todo
            }

            saveTodo?.let { todo ->
                // 전체 라벨에 투두 넣기
                _labelList.value?.find { it.order == 0 }?.let {
                    repository.insertTodo(todo, it)
                }

                // 선택된 라벨에 투두 넣기
                _selectedLabelList.value?.let { labels ->
                    labels.forEach { label ->
                        repository.insertTodo(todo, label)
                    }
                }

                _isSaveButtonEnabled.value = true
            }
        }
    }

    private fun getFormattedCurrentDate(date: Date): String {
        val simpleDateFormatDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        return simpleDateFormatDate.format(date)
    }

    private fun getFormattedCurrentTime(date: Date): String {
        val simpleDateFormatTime = SimpleDateFormat("a hh:mm", Locale.KOREA)
        return simpleDateFormatTime.format(date)
    }
}
