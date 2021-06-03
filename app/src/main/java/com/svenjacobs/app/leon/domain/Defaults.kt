/*
 * Léon - The URL Cleaner
 * Copyright (C) 2021 Sven Jacobs
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

package com.svenjacobs.app.leon.domain

import com.svenjacobs.app.leon.domain.model.Sanitizer.QueryParameterSanitizer
import com.svenjacobs.app.leon.domain.model.Sanitizer.RegexSanitizer

object Defaults {

    val SANITIZERS = listOf(
        QueryParameterSanitizer(
            parameterName = "wt_mc",
            name = "wt_mc",
            description = "Webtrekk",
            isDefault = true,
        ),
        RegexSanitizer(
            regex = "[?&](?:ga|utm)_[^=]*=.[^&]*",
            name = "ga_* & utm_*",
            description = "Urchin Tracking Module (Google Analytics)",
            isDefault = true,
        ),
    )
}