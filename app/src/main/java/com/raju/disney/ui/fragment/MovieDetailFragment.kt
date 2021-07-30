package com.raju.disney.ui.fragment

import TOAST
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import bindSrcUrl
import com.raju.disney.base.BaseFragment
import com.raju.disney.data.BookData
import com.raju.disney.databinding.FragmentMovieDetailBinding
import com.raju.disney.ui.fragment.viewmodel.MovieDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_detail.*

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment() {

  private val viewModel: MovieDetailViewModel by viewModels()
  private lateinit var binding : FragmentMovieDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.fetchBook(1308)
    viewModel.displayBookData.observeEvent(this, this::setBookData)
    viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    binding =
        FragmentMovieDetailBinding.inflate(LayoutInflater.from(activity), container, false)
    binding.viewModel = viewModel
    return binding.root
  }

  private fun setBookData(bookData: BookData) {
    val result = bookData.data.results[0]
    binding.bookData = bookData
    binding.result = result

    ivPoster.bindSrcUrl(result.thumbnail.imageThumbUri)
    ivBackdrop.bindSrcUrl(result.thumbnail.imageThumbUri)
    if (result.title.length > 10) {
      tvBookTitleValue.isSelected = true
    }
  }

  private fun showErrorMessage(message: String?) {
    TOAST(message)
  }

  companion object {
    @JvmStatic fun newInstance() = MovieDetailFragment()
  }
}
