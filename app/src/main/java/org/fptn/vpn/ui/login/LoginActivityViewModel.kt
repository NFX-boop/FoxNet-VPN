/*
 * FPTN Android Client
 * Copyright (C) 2026  Skokov Stanislav, Enin Sergey
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Website: https://fptn.org
 */

package com.foxnet.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.foxnet.app.database.AppDatabase
import com.foxnet.app.utils.SharedPrefUtils
import com.foxnet.app.utils.token.TokenUtils
import com.foxnet.app.exception.PVNClientException

class LoginActivityViewModel(application: Application) : AndroidViewModel(application) {

    val errorTextLiveData = MutableLiveData("")

    private val appDatabase = AppDatabase.getInstance(application)

    @Throws(PVNClientException::class)
    suspend fun parseAndSaveToken(token: String) {
        val serverEntities = TokenUtils.parseToken(token)
        withContext(Dispatchers.IO) {
            appDatabase.serverDAO().deleteAndInsert(serverEntities)
            SharedPrefUtils.saveTokenUpdatedDate(getApplication(), System.currentTimeMillis())
        }
    }
}
