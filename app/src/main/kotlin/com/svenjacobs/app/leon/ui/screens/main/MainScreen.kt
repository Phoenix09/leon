/*
 * Léon - The URL Cleaner
 * Copyright (C) 2022 Sven Jacobs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.svenjacobs.app.leon.ui.screens.main

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.svenjacobs.app.leon.R
import com.svenjacobs.app.leon.core.domain.CleanerService
import com.svenjacobs.app.leon.ui.common.views.TopAppBar
import com.svenjacobs.app.leon.ui.screens.main.model.MainScreenViewModel
import com.svenjacobs.app.leon.ui.screens.main.model.Screen
import com.svenjacobs.app.leon.ui.screens.main.views.BackgroundImage
import com.svenjacobs.app.leon.ui.screens.main.views.BottomBar
import com.svenjacobs.app.leon.ui.screens.main.views.BroomIcon
import com.svenjacobs.app.leon.ui.screens.settings.SettingsScreen
import com.svenjacobs.app.leon.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    var hideBars by rememberSaveable { mutableStateOf(false) }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    fun onShareButtonClick(cleaned: CleanerService.Result) {
        val intent = viewModel.buildIntent(cleaned.cleanedText)
        context.startActivity(intent)
    }

    fun onVerifyButtonClick(result: CleanerService.Result) {
        val intent = viewModel.buildCustomTabIntent(context)
        intent.launchUrl(context, Uri.parse(result.urls.first()))
    }

    fun onCopyToClipboardClick(result: CleanerService.Result) {
        clipboard.setText(AnnotatedString(result.cleanedText))
        coroutineScope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.clipboard_message))
        }
    }

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = !isDarkTheme)
    }

    AppTheme {
        Scaffold(
            modifier = modifier,
            topBar = { if (!hideBars) TopAppBar() },
            bottomBar = { if (!hideBars) BottomBar(navController = navController) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    BackgroundImage()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route,
                    ) {
                        composable(Screen.Main.route) {
                            Content(
                                result = uiState.result,
                                isUrlDecodeEnabled = uiState.isUrlDecodeEnabled,
                                onShareButtonClick = ::onShareButtonClick,
                                onVerifyButtonClick = ::onVerifyButtonClick,
                                onCopyToClipboardClick = ::onCopyToClipboardClick,
                                onUrlDecodeCheckedChange = viewModel::onUrlDecodeCheckedChange,
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onHideBars = { hideBars = it },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    result: CleanerService.Result?,
    isUrlDecodeEnabled: Boolean,
    onShareButtonClick: (CleanerService.Result) -> Unit,
    onVerifyButtonClick: (CleanerService.Result) -> Unit,
    onCopyToClipboardClick: (CleanerService.Result) -> Unit,
    onUrlDecodeCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BroomIcon(
                    modifier = Modifier
                        .size(128.dp)
                        .padding(
                            top = 16.dp,
                            bottom = 32.dp,
                        )
                )

                when (result) {
                    null -> HowToBody()
                    else -> SuccessBody(
                        result = result,
                        isUrlDecodeEnabled = isUrlDecodeEnabled,
                        onShareButtonClick = onShareButtonClick,
                        onVerifyButtonClick = onVerifyButtonClick,
                        onCopyToClipboardClick = onCopyToClipboardClick,
                        onUrlDecodeCheckedChange = onUrlDecodeCheckedChange,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SuccessBody(
    modifier: Modifier = Modifier,
    result: CleanerService.Result,
    isUrlDecodeEnabled: Boolean,
    onShareButtonClick: (CleanerService.Result) -> Unit,
    onCopyToClipboardClick: (CleanerService.Result) -> Unit,
    onVerifyButtonClick: (CleanerService.Result) -> Unit,
    onUrlDecodeCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.original_url),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = result.originalText,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.cleaned_url),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = result.cleanedText,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    OutlinedButton(
                        onClick = { onShareButtonClick(result) },
                    ) {
                        Text(
                            text = stringResource(R.string.share),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    OutlinedButton(
                        onClick = { onCopyToClipboardClick(result) },
                    ) {
                        Text(
                            text = stringResource(R.string.copy),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    OutlinedButton(
                        onClick = { onVerifyButtonClick(result) },
                    ) {
                        Text(
                            text = stringResource(R.string.verify),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.decode_url),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Switch(
                        modifier = Modifier.padding(start = 16.dp),
                        checked = isUrlDecodeEnabled,
                        onCheckedChange = onUrlDecodeCheckedChange,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HowToBody(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.how_to_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            Row {
                Image(
                    modifier = Modifier
                        .height(300.dp)
                        .padding(end = 16.dp),
                    painter = painterResource(R.drawable.howto_pixel_5),
                    contentDescription = stringResource(R.string.a11y_howto)
                )

                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Justify,
                    text = stringResource(R.string.how_to_text)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessBodyPreview() {
    AppTheme {
        SuccessBody(
            result = CleanerService.Result(
                originalText = "http://www.some.url?tracking=true",
                cleanedText = "http://www.some.url",
                urls = emptyList(),
            ),
            isUrlDecodeEnabled = false,
            onShareButtonClick = {},
            onVerifyButtonClick = {},
            onCopyToClipboardClick = {},
            onUrlDecodeCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FailureBodyPreview() {
    AppTheme {
        HowToBody()
    }
}
