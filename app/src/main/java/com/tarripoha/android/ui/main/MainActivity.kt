package com.tarripoha.android.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.di.module.MainActivityModule
import com.tarripoha.android.util.ViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelFactory

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        getDependency()
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
    }

    private fun getDependency() {
        val component: MainActivityComponent = DaggerMainActivityComponent
            .builder().applicationComponent(App.get(this)?.getComponent())
            .mainActivityModule(MainActivityModule(this))
            .build()
        component.injectMainActivity(this)
    }
}