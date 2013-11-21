package io.airlift.command;

import com.google.common.base.Joiner;
import org.testng.annotations.Test;

public class PingTest
{
    @Test
    public void test()
    {
        // simple command parsing example
        ping();
        ping("-c", "5");
        ping("--count", "9");

        // show help
        ping("-h");
        ping("--help");
    }

    private void ping(String... args)
    {
        System.out.println("$ ping " + Joiner.on(' ').join(args));
        Ping.main(args);
        System.out.println();
    }
}
