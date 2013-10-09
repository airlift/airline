package io.airlift.command;

import com.google.common.base.Joiner;
import org.testng.annotations.Test;

public class PingWithRequiredOptionTest {

    @Test
    public void test()
    {
        // missing parameter => show help
        ping();
    }

    private void ping(String... args)
    {
        System.out.println("$ ping " + Joiner.on(' ').join(args));
        PingWithRequiredOption.main(args);
        System.out.println();
    }
}
