package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.MetadataLoader;
import io.airlift.airline.model.SuggesterMetadata;
import io.airlift.airline.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static io.airlift.airline.ParserUtil.createInstance;

@Command(name = "suggest")
public class SuggestCommand
        implements Runnable, Callable<Void>
{
    private static final Map<Context, Class<? extends Suggester>> BUILTIN_SUGGESTERS = createBuiltinSuggesters();

    @Inject
    public GlobalMetadata metadata;

    @Arguments
    public List<String> arguments = new ArrayList<>();

    public Iterable<String> generateSuggestions()
    {
        Parser parser = new Parser();
        ParseState state = parser.parse(metadata, arguments);

        Class<? extends Suggester> suggesterClass = BUILTIN_SUGGESTERS.get(state.getLocation());
        if (suggesterClass != null) {
            SuggesterMetadata suggesterMetadata = MetadataLoader.loadSuggester(suggesterClass);

            if (suggesterMetadata != null) {

                Map<Class<?>, Object> bindings = new HashMap<>(3);
                bindings.put(GlobalMetadata.class, metadata);

                if (state.getGroup() != null) {
                    bindings.put(CommandGroupMetadata.class, state.getGroup());
                }

                if (state.getCommand() != null) {
                    bindings.put(CommandMetadata.class, state.getCommand());
                }

                Suggester suggester = createInstance(suggesterMetadata.getSuggesterClass(),
                        new ArrayList<>(),
                        null,
                        null,
                        null,
                        suggesterMetadata.getMetadataInjections(),
                        bindings);

                return suggester.suggest();
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void run()
    {
        System.out.println(CollectionUtils.asList(generateSuggestions()).stream().collect(Collectors.joining("\n")));
    }

    @Override
    public Void call()
    {
        run();
        return null;
    }

    private static Map<Context, Class<? extends Suggester>> createBuiltinSuggesters() {
        Map<Context, Class<? extends Suggester>> suggesters = new EnumMap<>(Context.class);
        suggesters.put(Context.GLOBAL, GlobalSuggester.class);
        suggesters.put(Context.GROUP, GroupSuggester.class);
        suggesters.put(Context.COMMAND, CommandSuggester.class);
        return suggesters;
    }
}
