package com.ap.roomdemo

import android.util.Patterns
import androidx.lifecycle.*
import com.ap.roomdemo.db.Subscriber
import com.ap.roomdemo.db.SubscriberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel() {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberUpdateOrDelete: Subscriber

    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage

    val inputName = MutableLiveData<String>()
    val inputEmail = MutableLiveData<String>()

    val saveOrUpdateButtonText = MutableLiveData<String>()
    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    init {
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "ClearAll"
    }

    fun getSavedSubscribers() = liveData {
        repository.subscribers.collect {
            emit(it)
        }
    }


    fun saveOrUpdate() {
        if (inputName.value == null) {
            statusMessage.value = Event("Please enter subscriber's name")
        } else if (inputEmail.value == null) {
            statusMessage.value = Event("Please enter subscriber's email")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()) {
            statusMessage.value = Event("Please enter correct email address")
        } else {
            if (isUpdateOrDelete) {
                subscriberUpdateOrDelete.name = inputName.value!!
                subscriberUpdateOrDelete.email = inputEmail.value!!
                update(subscriberUpdateOrDelete)
            } else {
                val name = inputName.value!!
                val email = inputEmail.value!!
                insert(Subscriber(0, name, email))

                inputName.value = ""
                inputEmail.value = ""
            }
        }

    }

    fun clearAllDelete() {
        if (isUpdateOrDelete)
            delete(subscriberUpdateOrDelete)
        else
            clearAll()
    }

    private fun insert(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val newRowId = repository.insert(subscriber)
        withContext(Dispatchers.Main) {
            if (newRowId > -1) {
                statusMessage.value = Event("Subscriber Inserted Successfully! $newRowId")
            } else {
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }

    private fun update(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val numberOfRows = repository.update(subscriber)
        withContext(Dispatchers.Main) {
            if (numberOfRows > 0) {
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "ClearAll"
                statusMessage.value = Event("$numberOfRows Rows Updated Successfully!")
            } else {
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }

    private fun delete(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val numberOfRowsDeleted = repository.delete(subscriber)
        withContext(Dispatchers.Main) {
            if (numberOfRowsDeleted > 0) {
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "ClearAll"
                statusMessage.value = Event("$numberOfRowsDeleted Rows Deleted Successfully!")
            } else {
                statusMessage.value = Event("Error Occurred!")

            }
        }
    }

    private fun clearAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
        withContext(Dispatchers.Main) {
            statusMessage.value = Event("All Subscribers deleted Successfully!")
        }
    }

    fun initUpdateAndDelete(subscriber: Subscriber) {
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberUpdateOrDelete = subscriber
        saveOrUpdateButtonText.value = "Update"
        clearAllOrDeleteButtonText.value = "Delete"
    }
}