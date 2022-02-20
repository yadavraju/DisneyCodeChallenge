package com.raju.disney.ui.fragment

import TOAST
import android.os.Bundle
import android.util.Log
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
    private var binding: FragmentMovieDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val span = otel.startWorkflow("MovieDetailFragment:fetchBook:Api")
        viewModel.fetchBook(1308)
        viewModel.displayBookData.observeEvent(this, this::setBookData)
        viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
        span.end()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentMovieDetailBinding.inflate(inflater, container, false)
        binding?.viewModel = viewModel
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.lifecycleOwner = viewLifecycleOwner
    }

    private fun setBookData(bookData: BookData) {
        val span = otel.startWorkflow("MovieDetailFragment:setBookData:toUI")
        val result = bookData.data.results[0]
        binding?.bookData = bookData
        binding?.result = result

        ivPoster.bindSrcUrl(result.thumbnail.imageThumbUri)
        ivBackdrop.bindSrcUrl(result.thumbnail.imageThumbUri)
        if (result.title.length > 10) {
            tvBookTitleValue.isSelected = true
        }
        span.end()
    }

    override fun onResume() {
        super.onResume()
        Log.w("Raju", "sessionId: " + otel.oTelSessionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun showErrorMessage(message: String?) {
        TOAST(message)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MovieDetailFragment()
    }
}
