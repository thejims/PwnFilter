package com.pwn9.PwnFilter.rules.action;

import com.pwn9.PwnFilter.FilterState;
import com.pwn9.PwnFilter.util.Patterns;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Execute a chain of commands by the player.
 *  * NOTE: This method needs to use runTask to operate on the player, as the bukkit API
 * calls are NOT thread-safe.
 */
@SuppressWarnings("UnusedDeclaration")
public class Actioncmdchain implements Action {
    String commands;

    public void init(String s)
    {
        commands = s;
    }

    public boolean execute(final FilterState state ) {
        state.cancel = true;
        String cmds = Patterns.replaceVars(commands, state);
        String cmdchain[] = cmds.split("\\|");

        if (state.getPlayer() != null ) {
            for (final String cmd : cmdchain) {
                state.addLogMessage("Helped " + state.playerName + " execute command: " + cmd);

                Bukkit.getScheduler().runTask(state.plugin, new BukkitRunnable() {
                    @Override
                    public void run() {
                        state.getPlayer().chat("/" + cmd);
                    }
                });
            }
            return true;
        } else {
            state.addLogMessage("Could not execute cmdchain on non-player.");
            state.setCancelled(true);
            return false;
        }
    }
}