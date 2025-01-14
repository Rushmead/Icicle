/*
 * MIT License
 *
 * Copyright (c) 2021 IceyLeagons and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.iceyleagons.icicle.commands.manager;

import com.google.common.base.Strings;
import lombok.Getter;
import net.iceyleagons.icicle.commands.CommandInjectionException;
import net.iceyleagons.icicle.commands.CommandService;
import net.iceyleagons.icicle.commands.annotations.Command;
import net.iceyleagons.icicle.commands.annotations.manager.CommandManager;
import net.iceyleagons.icicle.commands.annotations.manager.SubCommand;
import net.iceyleagons.icicle.commands.annotations.meta.Alias;
import net.iceyleagons.icicle.commands.annotations.meta.Usage;
import net.iceyleagons.icicle.commands.annotations.params.FlagOptional;
import net.iceyleagons.icicle.commands.annotations.params.Optional;
import net.iceyleagons.icicle.commands.command.CommandNotFoundException;
import net.iceyleagons.icicle.commands.command.RegisteredCommand;
import net.iceyleagons.icicle.commands.middleware.CommandMiddlewareTemplate;
import net.iceyleagons.icicle.core.Application;
import net.iceyleagons.icicle.core.translations.TranslationService;
import net.iceyleagons.icicle.core.utils.Defaults;
import net.iceyleagons.icicle.utilities.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Getter
public class RegisteredCommandManager implements CommandExecutor, TabCompleter {

    private final Application application;
    private final CommandService commandService;
    private final CommandManager commandManager;
    private final CommandRegistry commandRegistry;
    private final Class<?> clazz;
    private final Object origin;

    public RegisteredCommandManager(Application application, CommandService commandService, CommandManager commandManager, Class<?> clazz, Object origin) throws CommandInjectionException {
        this(application, commandService, commandManager, clazz, origin, false);
    }

    public RegisteredCommandManager(Application application, CommandService commandService, CommandManager commandManager, Class<?> clazz, Object origin, boolean subCommand) throws CommandInjectionException {
        this.application = application;
        this.commandRegistry = new CommandRegistry(this);
        this.commandService = commandService;
        this.commandManager = commandManager;
        this.clazz = clazz;
        this.origin = origin;

        scanForCommands();
        if (!subCommand) {
            this.getCommandService().getInjector().injectCommand(commandManager.value().toLowerCase(), this, this, null, null, null, null, Arrays.asList(clazz.isAnnotationPresent(Alias.class) ? clazz.getAnnotation(Alias.class).value() : new String[0]));
        }
    }

    private void scanForCommands() throws CommandInjectionException {
        for (Method declaredMethod : this.clazz.getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(Command.class)) continue;

            commandRegistry.registerCommand(declaredMethod, origin);
        }
        for (SubCommand subCommand : this.clazz.getAnnotationsByType(SubCommand.class)) {
            Class<?> clazz = subCommand.value();
            Object sc = application.getBeanManager().getBeanRegistry().getBeanNullable(clazz);
            application.getBeanManager().getBeanRegistry().unregisterBean(clazz);

            if (!(sc instanceof RegisteredCommandManager)) {
                throw new IllegalStateException("Annotation value: " + clazz.getName() + " points to a non existing SubCommand manager in " + this.clazz.getName() + " .");
            }

            commandRegistry.registerSubCommand((RegisteredCommandManager) sc);
        }

    }

    private Object[] getParams(Parameter[] parameters, String[] args, CommandSender commandSender) throws IllegalArgumentException {
        Object[] params = new Object[parameters.length];
        int argsCounter = 0;

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType().isArray() && param.getType().equals(String.class)) {
                Object[] array = new Object[args.length - argsCounter];
                for (int j = argsCounter; j < args.length; j++) {
                    array[j - argsCounter] = args[argsCounter];
                }

                params[i] = array; // TODO this needs testing
                break;
            } else if (param.isAnnotationPresent(net.iceyleagons.icicle.commands.annotations.params.CommandSender.class)) {
                params[i] = param.getType().isInstance(commandSender) ? param.getType().cast(commandSender) : null;
            } else if (param.isAnnotationPresent(FlagOptional.class)) {
                // TODO
                throw new IllegalStateException("FlagOptionals are currently not supported.");
            } else if (param.isAnnotationPresent(Optional.class)) {
                if (argsCounter < args.length) {
                    params[i] = this.commandService.resolveParameter(param.getType(), this, args[argsCounter++], commandSender);
                    continue;
                }
                params[i] = Defaults.DEFAULT_TYPE_VALUES.getOrDefault(param.getType(), null);
            } else {
                if (argsCounter < args.length) {
                    params[i] = this.commandService.resolveParameter(param.getType(), this, args[argsCounter++], commandSender);
                    continue;
                }

                throw new IllegalArgumentException("Too few arguments!");
            }
        }

        return params;
    }

    private String handleCommand(RegisteredCommand toExecute, String[] args, CommandSender commandSender) throws Exception {
        try {
            Object[] params = getParams(toExecute.getMethod().getParameters(), args, commandSender);
            return toExecute.execute(params);
        } catch (IllegalArgumentException e) {
            String usage;
            if (toExecute.getUsage() == null) {
                usage = toExecute.getDefaultUsage();
            } else {
                final TranslationService translationService = commandService.getTranslationService();
                final Usage usg = toExecute.getUsage();

                String key = Strings.emptyToNull(usg.key());
                usage = translationService.getTranslation(key, translationService.getLanguageProvider().getLanguage(commandSender), usg.defaultValue(),
                        Map.of("cmd", toExecute.getCommandName(), "sender", commandSender.getName()));
            }

            throw new IllegalArgumentException("Too few arguments!\nUsage: " + usage);
        }
    }

    private boolean handleCommand(RegisteredCommand registeredCommand, TranslationService translationService, CommandSender sender, String[] args) throws Exception {
        for (CommandMiddlewareTemplate middleware : commandService.getMiddlewareStore().getMiddlewares()) {
            if (!middleware.onCommand(this.commandManager, this.clazz, args[0].toLowerCase(), registeredCommand.getMethod(), sender, this.commandService.getTranslationService())) {
                return true;
            }
        }

        String[] newArgs = ArrayUtils.ignoreFirst(1, args);

        String response = handleCommand(registeredCommand, newArgs, sender);
        if (registeredCommand.isSuppliesTranslationKey()) {
            response = translationService.getTranslation(response, translationService.getLanguageProvider().getLanguage(sender), "");
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', response));
        return true;
    }

    private boolean handleSubCommand(RegisteredCommandManager manager, @NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String[] args) {
        String[] newArgs = ArrayUtils.ignoreFirst(1, args);
        return manager.onCommand(sender, command, manager.getCommandManager().value(), newArgs);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        final TranslationService translationService = commandService.getTranslationService();
        final String cmd = label.toLowerCase();
        if (!cmd.equalsIgnoreCase(this.commandManager.value())) return true;

        try {
            if (args.length < 1) {
                sender.sendMessage("Specify subcommand!");
                return true;
            }

            RegisteredCommand registeredCommand = this.commandRegistry.getCommand(args[0].toLowerCase());
            return handleCommand(registeredCommand, translationService, sender, args);
        } catch (CommandNotFoundException e) {
            try {
                RegisteredCommandManager registeredCommand = this.commandRegistry.getSubCommand(args[0].toLowerCase());
                handleSubCommand(registeredCommand, sender, command, args);
            } catch (CommandNotFoundException e2) {
                String errorMsgKey = Strings.emptyToNull(commandManager.notFound());
                String msg = translationService.getTranslation(errorMsgKey, translationService.getLanguageProvider().getLanguage(sender), "&cCommand &b{cmd} &cnot found!",
                        Map.of("cmd", e.getCommand(), "sender", sender.getName()));

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.RED + e.getMessage()));
            return true;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], commandRegistry.getCommands().keySet(), completions);
            Collections.sort(completions);
        } else {
            if (args.length > 1 && commandRegistry.getCommands().containsKey(args[0])) {
                try {
                    return commandRegistry.getCommand(args[0]).onTabComplete(sender, command, alias, ArrayUtils.ignoreFirst(1, args));
                } catch (Exception ignored) {
                }
            }
        }

        return completions;
    }
}
