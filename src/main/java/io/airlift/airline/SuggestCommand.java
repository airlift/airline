package io.airlift.airline;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.MetadataLoader;
import io.airlift.airline.model.OptionMetadata;
import io.airlift.airline.model.SuggesterMetadata;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.airlift.airline.ParserUtil.createInstance;

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
    public List<String> arguments = new ArrayList<>();

    @VisibleForTesting
    public Iterable<String> generateSuggestions()
    {
        Parser parser = new Parser();
        ParseState state = parser.parse(metadata, arguments);

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
                        bindings.build(),
                        new DefaultCommandFactory<Suggester>());

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
