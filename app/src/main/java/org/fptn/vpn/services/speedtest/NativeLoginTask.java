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

package com.foxnet.app.services.speedtest;

import com.google.gson.Gson;

import com.foxnet.app.database.entity.ServerEntity;
import com.foxnet.app.enums.BypassCensorshipMethod;
import com.foxnet.app.enums.SniSpoofingMode;
import com.foxnet.app.services.websocket.LoginRequest;
import com.foxnet.app.services.websocket.NativeHttpsClientImpl;
import com.foxnet.app.services.websocket.NativeResponse;
import org.json.JSONObject;

import com.foxnet.app.exception.ErrorCode;
import com.foxnet.app.exception.PVNClientException;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class NativeLoginTask implements Callable<SpeedTestResult> {
    private static final String LOGIN_URL = "/api/v1/login";
    private static final int TIMEOUT = 10;

    private final ServerEntity serverEntity;
    private final NativeHttpsClientImpl nativeHttpsClient;
    private final AtomicReference<PVNClientException> sharedAuthError;

    public NativeLoginTask(ServerEntity serverEntity, String sniHost, BypassCensorshipMethod censorshipStrategy, SniSpoofingMode sniSpoofingMode, AtomicReference<PVNClientException> sharedAuthError) {
        this.serverEntity = serverEntity;
        this.sharedAuthError = sharedAuthError;
        this.nativeHttpsClient = new NativeHttpsClientImpl(
                serverEntity.getHost(),
                serverEntity.getPort(),
                serverEntity.getMd5ServerFingerprint(),
                sniHost,
                censorshipStrategy,
                sniSpoofingMode
        );
    }

    @Override
    public SpeedTestResult call() throws Exception {
        String requestBody = new Gson().toJson(new LoginRequest(serverEntity.getUsername(), serverEntity.getPassword()));
        NativeResponse response = nativeHttpsClient.Post(LOGIN_URL, requestBody, TIMEOUT);
        if (response != null && response.code == 200) {
            JSONObject json = new JSONObject(response.body);
            String accessToken = json.getString("access_token");
            return new SpeedTestResult(serverEntity, accessToken);
        }
        if (response != null && response.code == 401) {
            PVNClientException ex = new PVNClientException(ErrorCode.ACCESS_TOKEN_ERROR);
            sharedAuthError.set(ex);
            throw ex;
        }
        throw new Exception("Login failed for " + serverEntity.getHost() + " [code=" + (response != null ? response.code : -1) + "]");
    }
}
