/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.JFA;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.build.CommandData;
import lonter.jfa.api.interactions.commands.build.Commands;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.CommandListUpdateAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.interactions.command.CommandImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class CommandListUpdateActionImpl extends RestActionImpl<List<Command>> implements CommandListUpdateAction {
    private final List<CommandData> commands = new ArrayList<>();
    private final GuildImpl guild;
    private int slash, user, message;

    public CommandListUpdateActionImpl(JFA api, GuildImpl guild, Route.CompiledRoute route) {
        super(api, route);
        this.guild = guild;
    }

    @NotNull
    @Override
    public CommandListUpdateAction timeout(long timeout, @NotNull TimeUnit unit) {
        return (CommandListUpdateAction) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public CommandListUpdateAction addCheck(@NotNull BooleanSupplier checks) {
        return (CommandListUpdateAction) super.addCheck(checks);
    }

    @NotNull
    @Override
    public CommandListUpdateAction setCheck(BooleanSupplier checks) {
        return (CommandListUpdateAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public CommandListUpdateAction deadline(long timestamp) {
        return (CommandListUpdateAction) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public CommandListUpdateAction addCommands(@NotNull Collection<? extends CommandData> commands) {
        Checks.noneNull(commands, "Command");
        int newSlash = 0, newUser = 0, newMessage = 0;
        for (CommandData command : commands) {
            switch (command.getType()) {
                case SLASH:
                    newSlash++;
                    break;
                case MESSAGE:
                    newMessage++;
                    break;
                case USER:
                    newUser++;
                    break;
            }
        }

        Checks.check(
                slash + newSlash <= Commands.MAX_SLASH_COMMANDS,
                "Cannot have more than %d slash commands! Try using subcommands instead.",
                Commands.MAX_SLASH_COMMANDS);
        Checks.check(
                user + newUser <= Commands.MAX_USER_COMMANDS,
                "Cannot have more than %d user context commands!",
                Commands.MAX_USER_COMMANDS);
        Checks.check(
                message + newMessage <= Commands.MAX_MESSAGE_COMMANDS,
                "Cannot have more than %d message context commands!",
                Commands.MAX_MESSAGE_COMMANDS);

        Checks.checkUnique(
                Stream.concat(commands.stream(), this.commands.stream()).map(c -> c.getType() + " " + c.getName()),
                "Cannot have multiple commands of the same type with identical names. "
                        + "Name: \"%s\" with type %s appeared %d times!",
                (count, value) -> {
                    String[] tuple = value.split(" ", 2);
                    return new Object[] {tuple[1], tuple[0], count};
                });

        slash += newSlash;
        user += newUser;
        message += newMessage;

        this.commands.addAll(commands);
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataArray json = DataArray.empty();
        json.addAll(commands);
        return getRequestBody(json);
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Command>> request) {
        List<Command> commands = response.getArray().stream(DataArray::getObject)
                .map(obj -> new CommandImpl(api, guild, obj))
                .collect(Collectors.toList());
        request.onSuccess(commands);
    }
}
