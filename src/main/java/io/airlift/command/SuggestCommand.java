/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;
import io.airlift.command.model.MetadataLoader;
import io.airlift.command.model.OptionMetadata;
import io.airlift.command.model.SuggesterMetadata;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static io.airlift.command.ParserUtil.createInstance;

@Command(name = "suggest")
public class SuggestCommand
        implements Runnable, Callable<Void>
{
    private static final Map<Context, Class<? extends Suggester>> BUILTIN_SUGGESTERS = ImmutableMap.<Context, Class<? extends Suggester>>builder()
            .put(Context.GLOBAL, GlobalSuggester.class)
            .put(Context.GROUP, GroupSuggester.class)
            .put(Context.COMMAND, CommandSuggester.class)
            .build();

    @Inject
    public GlobalMetadata metadata;

    @Arguments
    public List<String> arguments = newArrayList();

    @VisibleForTesting
    public Iterable<String> generateSuggestions()
    {
        Parser parser = new Parser(metadata);
        ParseState state = parser.parse(arguments);

        Class<? extends Suggester> suggesterClass = BUILTIN_SUGGESTERS.get(state.getLocation());
        if (suggesterClass != null) {
            SuggesterMetadata suggesterMetadata = MetadataLoader.loadSuggester(suggesterClass);

            if (suggesterMetadata != null) {
                ImmutableMap.Builder<Class<?>, Object> bindings = ImmutableMap.<Class<?>, Object>builder()
                        .put(GlobalMetadata.class, metadata);

                if (state.getGroup() != null) {
                    bindings.put(CommandGroupMetadata.class, state.getGroup());
                }

                if (state.getCommand() != null) {
                    bindings.put(CommandMetadata.class, state.getCommand());
                }

                Suggester suggester = createInstance(suggesterMetadata.getSuggesterClass(),
                        ImmutableList.<OptionMetadata>of(),
                        null,
                        null,
                        null,
                        suggesterMetadata.getMetadataInjections(),
                        bindings.build());

                return suggester.suggest();
            }
        }

        return ImmutableList.of();
    }

    @Override
    public void run()
    {
        System.out.println(Joiner.on("\n").join(generateSuggestions()));
    }

    @Override
    public Void call()
    {
        run();
        return null;
    }
}
