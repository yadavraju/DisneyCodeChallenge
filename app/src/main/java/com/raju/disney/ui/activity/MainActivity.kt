package com.raju.disney.ui.activity

import android.os.Bundle
import com.raju.disney.base.BaseActivity
import com.raju.disney.R
import com.raju.disney.ui.fragment.MovieDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      supportFragmentManager
          .beginTransaction()
          .add(R.id.fragmentContainer, MovieDetailFragment.newInstance())
          .commit()
    }
  }
}
