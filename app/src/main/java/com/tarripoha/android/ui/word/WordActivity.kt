package com.tarripoha.android.ui.word

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.ui.word.ui.theme.TarriPohaTheme
import com.tarripoha.android.ui.word.ui.theme.colorPrimary
import kotlinx.coroutines.launch

class WordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TarriPohaTheme {
                Surface(color = MaterialTheme.colors.background) {
                    if (intent.hasExtra(KEY_WORD)) {
                        val word = intent?.getParcelableExtra<Word>(KEY_WORD)
                        val displayMode = intent?.getStringExtra(KEY_MODE)
                            ?: KEY_MODE_NEW
                        LoginScreen(word, displayMode)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "WordActivityNew"
        const val KEY_WORD = "word"
        private const val KEY_MODE = "mode"
        const val KEY_MODE_NEW = "new"
        const val KEY_MODE_EDIT = "edit"

        fun getIntent(
            context: Context,
            word: Word,
            mode: String = KEY_MODE_NEW,
        ): Intent {
            val intent = Intent(context, WordActivity::class.java)
            intent.putExtra(KEY_MODE, mode)
            intent.putExtra(KEY_WORD, word)
            return intent
        }
    }
}

@Composable
fun LoginScreen(word: Word?, displayMode: String) {

    val activity = (LocalContext.current as Activity)

    var btnText = activity.getString(R.string.add)
    val title = if (displayMode == WordActivity.KEY_MODE_EDIT) {
        btnText = activity.getString(R.string.save)
        activity.getString(R.string.edit)
    } else {
        activity.getString(R.string.add_new_word)
    }

    val name = remember { mutableStateOf(word?.name ?: "") }
    val meaning = remember { mutableStateOf(word?.meaning ?: "") }
    val engMeaning = remember { mutableStateOf(word?.eng ?: "") }
    val desc = remember { mutableStateOf(word?.otherDesc ?: "") }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        TopAppBar(title = { Text(text = title) }, backgroundColor = colorPrimary)
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = name.value,
                enabled = false,
                onValueChange = {
                    name.value = it
                },
                maxLines = 1,
                label = { Text("${activity.getString(R.string.word)} *") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            Spacer(modifier = Modifier.padding(16.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = meaning.value,
                onValueChange = {
                    meaning.value = it
                },
                label = { Text("${activity.getString(R.string.meaning)} *") },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            Spacer(modifier = Modifier.padding(16.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = engMeaning.value,
                onValueChange = {
                    engMeaning.value = it
                },
                label = { Text(activity.getString(R.string.eng_meaning)) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            Spacer(modifier = Modifier.padding(16.dp))

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                value = desc.value,
                onValueChange = {
                    desc.value = it
                },
                label = { Text(activity.getString(R.string.other_description)) },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    localFocusManager.clearFocus()
                })
            )

            Spacer(modifier = Modifier.padding(16.dp))

        }

        Scaffold(
            scaffoldState = scaffoldState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (
                            name.value.trim()
                                .isEmpty() ||
                            meaning.value.trim()
                                .isEmpty()
                        ) {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = activity.getString(R.string.empty_field)
                                )
                            }
                            return@Button
                        }
                        val edit = word?.edit(
                            meaning = meaning.value.trim(),
                            engMeaning = engMeaning.value.trim(),
                            otherDesc = desc.value.trim()
                        )
                        val intent = Intent()
                        intent.putExtra(WordActivity.KEY_WORD, edit)
                        activity.setResult(AppCompatActivity.RESULT_OK, intent)
                        activity.finish()
                    }
                ) {
                    Text(text = btnText, fontSize = 16.sp)
                }
            }
        }
    }

    if (word == null) {
        ShowSnackbar(message = activity.getString(R.string.error_unknown))
    }
}

@Composable
fun ShowSnackbar(message: String) {
    Snackbar(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = message)
    }
}
