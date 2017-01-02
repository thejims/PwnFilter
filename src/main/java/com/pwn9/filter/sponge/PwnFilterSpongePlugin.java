/*
 *  PwnFilter - Chat and user-input filter with the power of Regex
 *  Copyright (C) 2016 Pwn9.com / Sage905 <sage905@takeflight.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.pwn9.filter.sponge;

import com.google.common.collect.MapMaker;
import com.pwn9.filter.sponge.config.SpongeConfig;
import com.pwn9.filter.sponge.listener.*;
import com.pwn9.filter.util.TemplateProvider;
import com.pwn9.filter.engine.FilterService;
import com.pwn9.filter.engine.rules.action.minecraft.MinecraftAction;
import com.pwn9.filter.engine.rules.action.targeted.TargetedAction;
import com.pwn9.filter.minecraft.api.MinecraftConsole;
import com.pwn9.filter.sponge.minecraft.command.pfcls;
import com.pwn9.filter.sponge.minecraft.command.pfmute;
import com.pwn9.filter.sponge.minecraft.command.pfreload;
import com.pwn9.filter.util.tag.RegisterTags;
//import net.milkbowl.vault.economy.Economy;


import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.asset.Asset;




import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.nio.file.Path;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;

/**
 * A Regular Expression (REGEX) Chat Filter For Sponge with many great features
 *
 * @author sage905
 * @version $Id: $Id
 */
@Plugin(id = "pwnfilter", name = "PwnFilter Sponge Plugin", version = "3.9.1", description = "A Regular Expression (REGEX) Chat Filter For Sponge with many great features")
public class PwnFilterSpongePlugin implements TemplateProvider {
    public static final ConcurrentMap<UUID, String> lastMessage = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();
    public static final ConcurrentMap<UUID, String> triggeredCommand = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();


    // static Economy economy = null;
    //private MCStatsTracker statsTracker; // who needs stats! didn't look too hard for a sponge equivalent.. doesn't really seem needed
    private static PwnFilterSpongePlugin _instance;
    private SpongeAPI minecraftAPI;
    private MinecraftConsole console;
    private FilterService filterService;

	@Inject @DefaultConfig(sharedRoot = false) private Path configFile;
    public Path getConfigFile() { return configFile; }

    @Inject @DefaultConfig(sharedRoot = false) private static ConfigurationLoader<CommentedConfigurationNode> configLoader;
	public static ConfigurationLoader getConfigLoader() { return configLoader; }
	
	@Inject	@ConfigDir(sharedRoot = false) private Path configDir;
    public Path getConfigDirectory() { return configDir; }

    public static Logger getLogger() {	return container.getLogger(); }

    @Inject private static final PluginContainer container = Sponge.getPluginManager().getPlugin("pwnfilter").get();

    /**
     * <p>Constructor for PwnFilter.</p>
     */
    public PwnFilterSpongePlugin() {

        if (_instance == null) {
            _instance = this;
        } else {
            throw new IllegalStateException("Only one instance of PwnFilter can be run per server");
        }
        minecraftAPI = new SpongeAPI(this);
        console = new MinecraftConsole(minecraftAPI);
        //statsTracker = new MCStatsTracker(this);
        filterService = new FilterService();
        filterService.getActionFactory().addActionTokens(MinecraftAction.class);
        filterService.getActionFactory().addActionTokens(TargetedAction.class);
        filterService.getConfig().setTemplateProvider(this);
        RegisterTags.all();

    }

    /**
     * <p>getInstance.</p>
     *
     * @return a {@link PwnFilterSpongePlugin} object.
     */
    public static PwnFilterSpongePlugin getInstance() {
        return _instance;
    }

    /**
     * <p>onServerInit</p>
     */
    @Listener
    public void onServerInit(GameInitializationEvent event) throws IOException {

        // Set up a Vault economy for actions like "fine" (optional)
        // setupEconomy();

        // Now get our configuration
        configurePlugin();
        //SpongeConfig.loadConfiguration();

        // Activate Statistics Tracking
        //statsTracker.startTracking();

        filterService.registerAuthorService(minecraftAPI);
        filterService.registerNotifyTarget(minecraftAPI);

        //Load up our listeners
        //        BaseListener.setAPI(minecraftAPI);
        filterService.registerClient(new PwnFilterCommandListener(this));
        //filterService.registerClient(new PwnFilterInvListener(this));
        filterService.registerClient(new PwnFilterPlayerListener(this));
        filterService.registerClient(new PwnFilterServerCommandListener(this));
        //filterService.registerClient(new PwnFilterSignListener(this));
        //filterService.registerClient(new PwnFilterBookListener(this));

        // Listeners that do not need to be reloaded for configuration changes to follow
        // The Entity Death handler, for custom death messages.
        Sponge.getEventManager().registerListeners(this, new PwnFilterEntityListener());
        // The DataCache handler, for async-safe player info (name/world/permissions)
        Sponge.getEventManager().registerListeners(this, new PlayerCacheListener());

        // Enable the listeners
        filterService.enableClients();
    }

    /**
     * <p>onServerStarting</p>
     */
	@Listener
    public void onServerStart(GameStartingServerEvent event) throws IOException {
        // Set up Command Handlers
		CommandSpec pfrCommandSpec = CommandSpec.builder()
			.description(Text.of("Reloads the config of PwnFilter"))
			.permission("pwnfilter.reload")
			.executor(new pfreload(filterService, this))
			.build();
		CommandSpec pfcCommandSpec = CommandSpec.builder()
			.description(Text.of("Clear screen of all chat globally"))
			.permission("pwnfilter.cls")
			.executor(new pfcls())
			.build();
		CommandSpec pfmCommandSpec = CommandSpec.builder()
			.description(Text.of("Mute all chat globally"))
			.permission("pwnfilter.mute")
			.executor(new pfmute())
			.build();

        Sponge.getCommandManager().register(getInstance(), pfrCommandSpec, "pfreload", "pfrel", "pfr");
        Sponge.getCommandManager().register(getInstance(), pfcCommandSpec, "pfcls", "pfc");
        Sponge.getCommandManager().register(getInstance(), pfmCommandSpec, "pfmute", "pfm");
    }

    /**
     * <p>onDisable.</p>
     */
    public void onDisable() {
		// Unregister all Sponge Event handlers.
		Sponge.getEventManager().unregisterPluginListeners(getInstance());
        filterService.shutdown();
        filterService.deregisterAuthorService(minecraftAPI);
    }

   /* private void setupEconomy() {

        if (Sponge.getServer().getPluginManager().getPlugin("Vault") != null) {
            ProviderRegistration<Economy> rsp = Sponge.getServiceManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                PwnFilterSpongePlugin.getLogger().info("Vault found. Enabling actions requiring Vault");
                return;
            }
        }
        PwnFilterSpongePlugin.getLogger().info("Vault dependency not found.  Disabling actions requiring Vault");
    } */
   public void configurePlugin() {
       minecraftAPI.reset();
       try {
            checkFiles();

            configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
            SpongeConfig.loadConfiguration();

           // Set the directory containing rules files.
           File ruleDir = setupDirectory(SpongeConfig.getRulesDirectory());
           if (ruleDir == null)
               throw new IOException("Unable to create or access rule directory.");
           filterService.getConfig().setRulesDir(ruleDir);

           // Set the directory containing Text Files
           File textDir = setupDirectory(SpongeConfig.getTextDirectory());
           if (textDir == null)
               throw new IOException("Unable to create or access rule directory.");
           filterService.getConfig().setTextDir(textDir);


           // Set up the default action messages.. do what now?
           //TargetedAction.getActionsWithDefaults().
           //filter(targetedAction -> !(rootNode.getNode(targetedAction.getDefaultMsgConfigName()) == null)).
           //forEach(targetedAction -> targetedAction.setDefMsg(
           //ChatColor.translateAlternateColorCodes('&',
           //rootNode.getNode(targetedAction.getDefaultMsgConfigName())))
           //);

           //setupPoints(filterService);
       } catch (IOException e) {
            getLogger().error(e.toString());
       }
   }


    /* private static void setupPoints(FilterService filterService) {
        PointManager pointManager = filterService.getPointManager();
        ConfigurationNode pointsSection = SpongeConfig.getPoints();
        if (!pointsSection.getNode("enabled").getBoolean()) {
            if (pointManager.isEnabled()) {
                pointManager.shutdown();
            }
        } else {
            if (!pointManager.isEnabled()) {
                pointManager.setLeakPoints(pointsSection.getNode("leak", "points").getDouble());
                pointManager.setLeakInterval(pointsSection.getNode("leak", "interval").getInt());

                try {
                    parseThresholds(pointsSection.getNode("thresholds"), pointManager, filterService.getActionFactory());
                } catch (InvalidActionException ex) {
                    PwnFilterSpongePlugin.getLogger().warning("Invalid Action parsing Thresholds: " + ex.getMessage());
                    pointManager.shutdown();
                }
                pointManager.start();
            }
        }
    }

    private static void parseThresholds(ConfigurationNode configSection,
                                        PointManager pointManager,
                                        ActionFactory actionFactory)
            throws InvalidActionException {

        for (ConfigurationNode threshold : configSection.getChildrenList()) {
            PwnFilterSpongePlugin.getLogger().
                    finest("Parsing Threshold: " + threshold.getString());

            List<Action> ascending = new ArrayList<>();
            List<Action> descending = new ArrayList<>();

            for (ConfigurationNode action : threshold.getNode("actions", "ascending").getChildrenList()) {
                PwnFilterSpongePlugin.getLogger().
                        finest("Adding Ascending Action: " + action.getString());
                ascending.add(actionFactory.getActionFromString(action.getString()));
            }
            for (ConfigurationNode action : threshold.getNode("actions", "descending").getChildrenList()) {
                PwnFilterSpongePlugin.getLogger().
                        finest("Adding Descending Action: " + action.getString());
                descending.add(actionFactory.getActionFromString(action.getString()));
            }
            pointManager.addThreshold(
                    threshold.getNode("name").getString(),
                    threshold.getNode("points").getDouble(),
                    ascending,
                    descending);
            PwnFilterSpongePlugin.getLogger().
                    finest("Adding Threshold: " + threshold.getNode("name").getString() +
                            " at points: " + threshold.getNode("points").getDouble());
        }

    } */

    /**
     * Ensure that the named directory exists and is accessible.  If the
     * directory begins with a / (slash), it is assumed to be an absolute
     * path.  Otherwise, the directory is assumed to be relative to the root
     * data folder.
     * <p/>
     * If the directory doesn't exist, an attempt is made to create it.
     *
     * @param directoryName relative or absolute path to the directory
     * @return {@link File} referencing the directory.
     */
    @Nullable
    private File setupDirectory(@NotNull String directoryName) {
        File dir;
        if (directoryName.startsWith("/")) {
            dir = new File(directoryName);
        } else {
            dir = new File(configDir.toFile(), directoryName);
        }
        try {
            if (!dir.exists()) {
                if (dir.mkdirs())
                    getLogger().info("Created directory: " + dir.getAbsolutePath());
            }
            return dir;
        } catch (Exception ex) {
            getLogger().warn("Unable to access or create directory: " + dir.getAbsolutePath());
            return null;
        }
    }
    private void checkFiles() {
        // Files that should be found in the assets directory, default files if you will..
        String[] assetFiles = {"pwnfilter.conf", "rules/book.txt", "rules/chat.txt", "rules/command.txt", "rules/console.txt", "rules/item.txt", "rules/sign.txt"};

        for (String assetFile:assetFiles) {
            File targetFile = new File(configDir.toString() + "/" + assetFile);
            if (targetFile.exists()) {
                continue;
            }
            getLogger().info(assetFile + " is missing, copying from jarfile");
            Optional<Asset> asset = container.getAsset(assetFile);
            if (!asset.isPresent()) {
                getLogger().info("Possible corrupt jar file, unable to locate " + assetFile);
            } else {
                try {
                    asset.get().copyToFile(targetFile.toPath());
                } catch (IOException e) {
                    getLogger().info("Error while copying file " + assetFile + ":" + e.getMessage());
                }
            }
        }
    }

    @Override
	public InputStream getResource(String filename) {
        getLogger().info("getResource called for: " + filename);
        getLogger().info("Class: " + getClass().toString());
        getLogger().info("URL:   " + getClass().getResource(filename));
        return getClass().getResourceAsStream(filename);
	}
	
    public FilterService getFilterService() {
        return filterService;
    }

    public MinecraftConsole getConsole() {
        return console;
    }

}