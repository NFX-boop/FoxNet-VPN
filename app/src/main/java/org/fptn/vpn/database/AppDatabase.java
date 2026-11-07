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

package com.foxnet.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.foxnet.app.database.dao.AppInfoDAO;
import com.foxnet.app.database.dao.ServerDAO;
import com.foxnet.app.database.dao.SniDao;
import com.foxnet.app.database.entity.AppInfoEntity;
import com.foxnet.app.database.entity.ServerEntity;
import com.foxnet.app.database.entity.SniEntity;

@Database(entities = {ServerEntity.class, AppInfoEntity.class, SniEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String FPTN_DATABASE = "FptnDatabase";

    public abstract ServerDAO serverDAO();

    public abstract AppInfoDAO appInfoDAO();

    public abstract SniDao sniDAO();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, FPTN_DATABASE)
                    .fallbackToDestructiveMigration(true)
                    .build();
        }
        return instance;
    }

}
