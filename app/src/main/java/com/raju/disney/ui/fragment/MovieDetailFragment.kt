package com.raju.disney.ui.fragment

import TOAST
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.raju.disney.base.BaseFragment
import com.raju.disney.databinding.FragmentMovieDetailBinding
import com.raju.disney.ui.fragment.viewmodel.MainViewModel
import com.raju.disney.util.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main.*

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.fetchBook(1308)
    viewModel.showDownloadDialog.observeEvent(this, this::openPopUpDialogFragment)
    viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val binding =
        FragmentMovieDetailBinding.inflate(LayoutInflater.from(activity), container, false)
    binding.viewModel = viewModel
    return binding.root
  }

  private fun showErrorMessage(message: String?) {
    TOAST(message)
  }

  private fun openPopUpDialogFragment(downloadUrl: String?) {
    activity?.let { downloadUrl?.let { dUrl -> DialogUtils.showSaveGiphyDialogue(it, dUrl) } }
  }

  companion object {

    @JvmStatic fun newInstance() = MovieDetailFragment()
  }
}
