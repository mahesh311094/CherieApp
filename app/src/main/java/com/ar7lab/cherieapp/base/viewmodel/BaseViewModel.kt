package com.ar7lab.cherieapp.base.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ar7lab.cherieapp.BuildConfig
import com.ar7lab.cherieapp.base.exception.ServerException
import com.ar7lab.cherieapp.base.extension.asLiveData
import com.ar7lab.cherieapp.network.response.ErrorWithSessionExpired
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.net.SocketTimeoutException
import kotlin.properties.Delegates

/**
 * A common class for ViewModel to handle common functionality
 */
abstract class BaseViewModel<ViewState : BaseViewState, ViewAction : BaseAction>(initialState: ViewState) :
    ViewModel() {

    /**
     * Define liveData to save the state of View (Activity/Fragment)
     */
    private val stateMutableLiveData = MutableLiveData<ViewState>()
    val stateLiveData = stateMutableLiveData.asLiveData()

    private var stateTimeTravelDebugger: StateTimeTravelDebugger? = null

    init {
        if (BuildConfig.DEBUG) {
            stateTimeTravelDebugger = StateTimeTravelDebugger(this::class.java.simpleName)
        }
    }

    // Delegate will handle state event deduplication
    // (multiple states of the same type holding the same data will not be dispatched multiple time to LiveData stream)
    protected var state by Delegates.observable(initialState) { _, old, new ->
        stateMutableLiveData.value = new

        if (new != old) {
            stateTimeTravelDebugger?.apply {
                addStateTransition(old, new)
                logLast()
            }
        }
    }

    fun sendAction(viewAction: ViewAction) {
        stateTimeTravelDebugger?.addAction(viewAction)
        state = onReduceState(viewAction)
    }

    fun loadData() {
        onLoadData()
    }

    protected open fun onLoadData() {}

    protected abstract fun onReduceState(viewAction: ViewAction): ViewState

    /**
     * Parsing the exception and return readable error message
     * @param throwable from try catch block
     * @return readable error message as String
     */
    protected fun getErrorMessage(throwable: Throwable): ErrorWithSessionExpired {
        if (throwable is HttpException) {
            return getHttpExceptionMessage(throwable)
        }
        if (throwable is SocketTimeoutException) {
            return ErrorWithSessionExpired("Server busy! Please try again.",false)
        }
        return ErrorWithSessionExpired(throwable.message ?: "Unknown error!",false)
    }

    /**
     * Parsing error message returns by server with an pre-defined format
     * @param httpException from try catch block
     * @return message from header response of a http request
     */
    private fun getHttpExceptionMessage(httpException: HttpException): ErrorWithSessionExpired {
        try {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(ServerException::class.java)

            val serverException =
                jsonAdapter.fromJson(httpException.response()?.errorBody()?.string() ?: "")
                    ?: return ErrorWithSessionExpired("${httpException.code()}! Server Error",false)
            /**
             * This is special case because in sign up function, the server returns this format
             * Have to parse manually from each errors message
             */
            if (httpException.code() == 422) {
                serverException.data?.let {
                    var message = ""
                    it.errors.forEach { error ->
                        message += "${error.msg}\n"
                    }
                    return ErrorWithSessionExpired(message,it.sessionExpired)
                }
            }
            return ErrorWithSessionExpired(serverException.message,serverException.data?.sessionExpired?:false)
        } catch (e: Exception) {
            e.printStackTrace()
            return ErrorWithSessionExpired("",false)
        }
    }
}