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

package com.svenjacobs.app.leon.ui.screens.main.model

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svenjacobs.app.leon.R
import com.svenjacobs.app.leon.core.domain.CleanerService
import com.svenjacobs.app.leon.core.domain.CleanerService.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val cleanerService: CleanerService,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isUrlDecodeEnabled: Boolean = false,
        val result: Result? = null,
    )

    private val text = MutableStateFlow<String?>(null)
    private val urlDecodeEnabled = MutableStateFlow(false)

    val uiState =
        combine(
            text,
            urlDecodeEnabled,
        ) { text, urlDecodeEnabled ->
            val (result, isError) = try {
                text?.let {
                    Pair(
                        cleanerService.clean(
                            text = text,
                            decodeUrl = urlDecodeEnabled,
                        ),
                        false,
                    )
                } ?: Pair(null, false)
            } catch (e: Exception) {
                Pair(null, true)
            }

            UiState(
                isLoading = text == null,
                isError = isError,
                isUrlDecodeEnabled = urlDecodeEnabled,
                result = result,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    fun setText(text: String?) {
        if (text == null && uiState.value.result != null) return
        this.text.value = text
    }

    fun onUrlDecodeCheckedChange(enabled: Boolean) {
        urlDecodeEnabled.value = enabled
    }

    fun buildIntent(text: String): Intent {
        val target = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        return Intent.createChooser(target, null)
    }

    fun buildCustomTabIntent(context: Context): CustomTabsIntent {
        val toolbarColorLight = ContextCompat.getColor(context, R.color.primaryColor)
        val toolbarColorDark = ContextCompat.getColor(context, R.color.nightPrimaryColor)

        val light = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(toolbarColorLight)
            .build()

        val dark = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(toolbarColorDark)
            .build()

        return CustomTabsIntent.Builder()
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, dark)
            .setDefaultColorSchemeParams(light)
            .build()
    }
}
