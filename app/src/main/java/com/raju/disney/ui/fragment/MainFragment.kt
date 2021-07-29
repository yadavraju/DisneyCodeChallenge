package com.raju.disney.ui.fragment

import TOAST
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.Nullable
import androidx.fragment.app.viewModels
import com.raju.disney.base.BaseFragment
import com.raju.disney.databinding.FragmentMainBinding
import com.raju.disney.ui.fragment.viewmodel.MainViewModel
import com.raju.disney.util.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.util.*
import kotlinx.android.synthetic.main.fragment_main.*

@AndroidEntryPoint
public class MainFragment : BaseFragment() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(@Nullable savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.showDownloadDialog.observeEvent(this, this::openPopUpDialogFragment)
    viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val binding = FragmentMainBinding.inflate(LayoutInflater.from(activity), container, false);
    binding.viewModel = viewModel
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    searchPhrase.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        return@setOnEditorActionListener true
      }
      false
    }
  }

  private fun showErrorMessage(message: String?) {
    TOAST(message)
  }

  private fun openPopUpDialogFragment(downloadUrl: String?) {
    activity?.let { downloadUrl?.let { dUrl -> DialogUtils.showSaveGiphyDialogue(it, dUrl) } }
  }

  companion object {
    fun newInstance(): MainFragment = MainFragment()
  }
}
