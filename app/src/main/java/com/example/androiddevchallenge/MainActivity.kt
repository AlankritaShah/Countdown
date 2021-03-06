/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.util.concurrent.TimeUnit

@ExperimentalAnimationApi
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp(CountDownViewModel())
            }
        }
    }
}

class CountDownViewModel : ViewModel() {
    val countDownDisplayProgress = MutableLiveData("")
    val countDownProgress = MutableLiveData(1.0)
    val isRunning = MutableLiveData(false)
    var countDown: CountDownTimer? = null

    fun startCountDown(timeInMillis: Long) {
        isRunning.value = true
        countDown = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hrFormat = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minFormat =
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    )
                val secFormat =
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )

                countDownDisplayProgress.value =
                    String.format("%02d:%02d:%02d", hrFormat, minFormat, secFormat)
                countDownProgress.value =
                    (millisUntilFinished.toDouble() / 1000) / (timeInMillis / 1000)
            }

            override fun onFinish() {
                isRunning.value = false
                countDownProgress.value = 1.0
                countDownDisplayProgress.value = ""
            }
        }.start()
    }

    fun endCountDown() {
        isRunning.value = false
        countDown?.cancel()
    }
}

@Composable
fun ShowProgress(value: Double, displayTime: String) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = value.toFloat(),
            modifier = Modifier.width(250.dp).height(250.dp),
            strokeWidth = 20.dp,
            color = Color.DarkGray
        )

        Text(text = displayTime)
    }
}

@ExperimentalAnimationApi
@Composable
fun MyApp(viewModel: CountDownViewModel) {
    val isRunning: Boolean by viewModel.isRunning.observeAsState(false)
    val countDownProgress: Double by viewModel.countDownProgress.observeAsState(1.0)
    val countDownDisplayProgress: String by viewModel.countDownDisplayProgress.observeAsState("")

    val hr = remember { mutableStateOf("0") }
    val min = remember { mutableStateOf("0") }
    val sec = remember { mutableStateOf("0") }

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))

            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn(initialAlpha = 0.5f),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                ShowProgress(countDownProgress, countDownDisplayProgress)
            }

            Spacer(Modifier.height(60.dp))

            Row {
                OutlinedTextField(
                    value = hr.value,
                    onValueChange = { hr.value = it },
                    label = { Text(text = "Hours") },
                    singleLine = true,
                    modifier = Modifier.weight(0.3f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        autoCorrect = false
                    )
                )
                Spacer(Modifier.width(4.dp))
                OutlinedTextField(
                    value = min.value,
                    onValueChange = { min.value = it },
                    label = { Text(text = "Minutes") },
                    singleLine = true,
                    modifier = Modifier.weight(0.3f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        autoCorrect = false
                    )
                )
                Spacer(Modifier.width(4.dp))
                OutlinedTextField(
                    value = sec.value,
                    onValueChange = { sec.value = it },
                    label = { Text(text = "Seconds") },
                    singleLine = true,
                    modifier = Modifier.weight(0.3f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        autoCorrect = false
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val totalSeconds = sec.value.toLong() * 1000
                    val totalMinutes = min.value.toLong() * 60000
                    val totalHours = hr.value.toLong() * 3600000
                    val total: Long = totalSeconds + totalMinutes + totalHours

                    if (!isRunning) {
                        viewModel.startCountDown(total)
                    } else {
                        viewModel.endCountDown()
                    }
                }
            ) {
                if (isRunning) {
                    Text("Stop")
                } else {
                    Text("Start")
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(CountDownViewModel())
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp(CountDownViewModel())
    }
}