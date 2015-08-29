package io.airlift.airline;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestPing
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
        System.out.println("$ ping " + Arrays.asList(args).stream().collect(Collectors.joining(" ")));
        Ping.main(args);
        System.out.println();
    }
}
