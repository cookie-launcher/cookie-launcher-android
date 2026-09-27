/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
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
 */
package com.cookielauncher.core.download.fabric;

import com.cookielauncher.core.download.ComponentVersionList;
import com.cookielauncher.core.download.DownloadProvider;
import com.cookielauncher.core.mod.RemoteMod;
import com.cookielauncher.core.mod.modrinth.ModrinthRemoteModRepository;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.core.util.Lang;

import java.util.Collections;

public class FabricAPIVersionList extends ComponentVersionList<FabricAPIRemoteVersion> {

    private final DownloadProvider downloadProvider;

    public FabricAPIVersionList(DownloadProvider downloadProvider) {
        this.downloadProvider = downloadProvider;
    }

    @Override
    public boolean hasType() {
        return false;
    }

    @Override
    public Task<?> refreshAsync() {
        return Task.runAsync(() -> {
            for (RemoteMod.Version modVersion : Lang.toIterable(ModrinthRemoteModRepository.MODS.getRemoteVersionsById("P7dR8mSH"))) {
                for (String gameVersion : modVersion.gameVersions()) {
                    versions.put(gameVersion, new FabricAPIRemoteVersion(gameVersion, modVersion.version(), modVersion.name(), modVersion.datePublished(), modVersion,
                            Collections.singletonList(modVersion.file().url())));
                }
            }
        });
    }
}
