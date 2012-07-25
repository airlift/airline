package org.iq80.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.MetadataLoader;
import org.iq80.cli.model.OptionMetadata;
import org.iq80.cli.model.SuggesterMetadata;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;
import static org.iq80.cli.ParserUtil.createInstance;

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
