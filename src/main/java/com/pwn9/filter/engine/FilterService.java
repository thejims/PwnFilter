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

package com.pwn9.filter.engine;

import com.google.common.collect.Sets;
import com.pwn9.filter.engine.api.*;
import com.pwn9.filter.engine.rules.action.ActionFactory;
import com.pwn9.filter.engine.rules.chain.InvalidChainException;
import com.pwn9.filter.engine.rules.chain.RuleChain;
import com.pwn9.filter.engine.rules.parser.TextConfigParser;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.pwn9.filter.sponge.PwnFilterSpongePlugin;
import org.slf4j.Logger;
import java.util.stream.Collectors;


/**
 * Handle Startup / Shutdown / Configuration of our PwnFilter Clients
 * User: Sage905
 * Date: 13-09-29
 * Time: 9:25 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author Sage905
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class FilterService {

    private final FilterConfig config;
    private final Set<FilterClient> registeredClients = Sets.newCopyOnWriteArraySet();
    private final Set<NotifyTarget> notifyTargets = Sets.newCopyOnWriteArraySet();
    private final ActionFactory actionFactory;
    private final PointManager pointManager = new PointManager(this);
    // Author Lookup Service
    private final List<AuthorService> authorServices = new CopyOnWriteArrayList<>();

    public FilterService() {
        this.config = new FilterConfig();
        this.actionFactory = new ActionFactory(this);
    }

    public PointManager getPointManager() {
        return pointManager;
    }

    @SuppressWarnings("WeakerAccess")
    public Set<FilterClient> getActiveClients() {
        return Collections.unmodifiableSet(registeredClients
                .stream().filter(FilterClient::isActive)
                .collect(Collectors.toSet()));
    }

    public void shutdown() {
        unregisterAllClients();
    }

    /**
     * <p>Getter for the field <code>registeredClients</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Set<FilterClient> getRegisteredClients() {
        return Collections.unmodifiableSet(registeredClients);
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void enableClients() {
        registeredClients.forEach(FilterClient::activate);

    }

    public void disableClients() {
        getActiveClients().forEach(FilterClient::shutdown);
    }

    /**
     * Add a listener to the PwnFilter ListenerManager.  This allows PwnFilter
     * to notify the listener when it should try to activate or shutdown.
     * PwnFilter will call the activate / shutdown methods when reloading
     * rules configs.
     * <p>
     * The FilterListener must register *before* attempting to use any other
     * PwnFilter resources.
     *
     * @param f FilterListener instance
     */
    public void registerClient(FilterClient f) {
        if (registeredClients.contains(f)) {
            return;
        }
        registeredClients.add(f);
    }

    /**
     * Remove a listener from the PwnFilter ListenerManager.
     * The listener will no longer be activated / deactivated when PwnFilter
     * reloads configs, rules, etc.
     * IMPORTANT: Before de-registering, the FilterListener must remove all
     * references to RuleSets.
     *
     * @param f FilterListener to remove.
     * @return true if the listener was previously registered and successfully
     * removed. False if it was not registered.
     */
    public boolean unregisterClient(FilterClient f) {
        if (registeredClients.contains(f)) {
            registeredClients.remove(f);
            return true;
        } else {
            return false;
        }
    }

    public void registerNotifyTarget(NotifyTarget t) {
        notifyTargets.add(t);
    }

    public void notifyTargets(String perm, String message) {
        PwnFilterSpongePlugin.getLogger().trace("Notify perm: " + perm + " Message: " + message);
        notifyTargets.forEach(target -> target.notifyWithPerm(perm, message));
    }

    /**
     * <p>unregisterAllClients.</p>
     */
    private void unregisterAllClients() {
        for (FilterClient f : registeredClients) {
            f.shutdown();
            registeredClients.remove(f);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disableClients();
    }

    public RuleChain parseRules(File ruleFile) throws InvalidChainException {
        TextConfigParser parser = new TextConfigParser(this);

        return parser.parse(ruleFile);
    }

    /*
     * GETTERS
     */
    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    public void registerAuthorService(AuthorService authorService) {
        authorServices.add(authorService);
    }

    public void deregisterAuthorService(AuthorService authorService) {
        authorServices.remove(authorService);
    }

    public void clearAuthorServices() {
        authorServices.clear();
    }

    public List<AuthorService> getAuthorServices() {
        return Collections.unmodifiableList(authorServices);
    }

    public MessageAuthor getAuthor(UUID uuid) {
        if (authorServices.isEmpty())
            throw new RuntimeException("No AuthorServices Registered. This should not happen!");
        for (AuthorService a : authorServices) {
            MessageAuthor author = a.getAuthorById(uuid);
            if (author != null) {
                return author;
            }
        }
        return new UnknownAuthor(uuid);
    }

}
