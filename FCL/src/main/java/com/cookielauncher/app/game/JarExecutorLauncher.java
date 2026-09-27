package com.cookielauncher.app.game;

import android.content.Context;

import com.mio.JavaManager;
import com.mio.manager.RendererManager;
import com.cookielauncher.app.setting.Profiles;
import com.cookielauncher.bridge.FCLConfig;
import com.cookielauncher.bridge.FCLauncher;
import com.cookielauncher.bridge.bridge.FCLBridge;
import com.cookielauncher.bridge.utils.FCLPath;
import com.cookielauncher.core.launch.CacioJavaArgs;
import com.cookielauncher.core.util.StringUtils;
import com.cookielauncher.core.util.platform.CommandBuilder;
import com.cookielauncher.core.util.platform.MemoryUtils;

import java.io.IOException;
import java.util.List;

public class JarExecutorLauncher {

    private final Context context;

    private String destJarPath;
    private int javaVersion;

    public JarExecutorLauncher(Context context) {
        this.context = context;
    }

    public void setInfo(String destJarPath, int javaVersion) {
        this.destJarPath = destJarPath;
        this.javaVersion = javaVersion;
    }

    private CommandBuilder generateCommandLine(String args) {
        CommandBuilder res = new CommandBuilder();

        CacioJavaArgs.addCacioArgs(
                res,
                FCLBridge.DEFAULT_WIDTH + "x" + FCLBridge.DEFAULT_HEIGHT,
                "javax.swing.plaf.metal.MetalLookAndFeel",
                null,
                true,
                javaVersion == 8
        );

        res.addDefault("-Xms", MemoryUtils.findBestRAMAllocation(context) + "m");
        res.addDefault("-Xmx", MemoryUtils.findBestRAMAllocation(context) + "m");

        res.addDefault("-Duser.home=", Profiles.getSelectedProfile().getGameDir().getParent());
        res.addDefault("-Djava.io.tmpdir=", FCLPath.CACHE_DIR);
        res.addDefault("-Dorg.lwjgl.opengl.libname=", "${gl_lib_name}");

        if (args != null) {
            for (String s : args.split(" ")) {
                res.add(s);
            }
        } else {
            res.add("-jar");
            res.add(destJarPath);
        }
        return res;
    }

    public FCLBridge launch(String args) throws IOException, InterruptedException {
        final CommandBuilder command = generateCommandLine(args);

        List<String> rawCommandLine = command.asList();

        if (rawCommandLine.stream().anyMatch(StringUtils::isBlank)) {
            throw new IllegalStateException("Illegal command line " + rawCommandLine);
        }

        String[] finalArgs = rawCommandLine.toArray(new String[0]);

        FCLConfig config = new FCLConfig(
                context,
                FCLPath.LOG_DIR,
                JavaManager.getSuitableJavaVersion(javaVersion).getJavaPath(null),
                Profiles.getSelectedProfile().getGameDir().getAbsolutePath(),
                RendererManager.RENDERER_GL4ES,
                finalArgs
        );
        return FCLauncher.launchJarExecutor(config);
    }
}
