package com.example.imageeditor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MainActivityTest{
    private lateinit var mainActivity: MainActivity

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setObject(){
        mainActivity = MainActivity()
    }

    @Test
    fun checkCalculation(){
       // val result = mainActivity.selectImage(3, 7)
       // assertThat(result).isEqualTo(10)
    }



}