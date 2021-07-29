package com.raju.disney.ui.fragment.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.raju.disney.api.repository.GiphyRepository
import com.raju.disney.base.BaseViewModel
import com.raju.disney.data.GData
import com.raju.disney.ui.adapter.CommonAdapter
import com.raju.disney.ui.factory.AppFactory
import com.raju.disney.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val TAG: String = "MainViewModel"

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repository: GiphyRepository,
    private val adapter: CommonAdapter,
    private val appFactory: AppFactory
) : BaseViewModel() {

  private val adapterObservableField: ObservableField<CommonAdapter> = ObservableField()
  private val loadingObservableField: ObservableField<Boolean> = ObservableField()

  val showErrorMessage: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }
  val showDownloadDialog: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }

  fun fetchBook(comicId: Int) {
    viewModelScope.launch {
      loadingObservableField.set(true)
      repository
          .getBookData(comicId)
          .catch { e -> handleException(TAG, e, loadingObservableField) }
          .collect {
            loadingObservableField.set(true)
            Log.e("raju", it.toString())
          }
    }
  }

  private fun setAdapterData(giphyGDataList: List<GData>) {
    val viewModels = giphyGDataList.map { appFactory.createGiphyAdapter(it, ::onImageLongPress) }
    adapter.setDataBoundAdapter(viewModels)
    adapterObservableField.set(adapter)
    loadingObservableField.set(false)
  }

  private fun onImageLongPress(uri: String?) {
    showDownloadDialog.value = uri
  }

  fun getViewModelAdapter(): ObservableField<CommonAdapter> = adapterObservableField

  fun isLoading(): ObservableField<Boolean> = loadingObservableField

  override fun showExceptionMessage(message: String?) {
    showErrorMessage.value = message
  }
}
