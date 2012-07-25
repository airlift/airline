package org.iq80.cli;

import com.google.common.base.Joiner;
import org.testng.annotations.Test;

public class GitTest
{
    @Test
    public void test()
    {
        // simple command parsing example
        git("add", "-p", "file");
        git("remote", "add", "origin", "git@github.com:dain/git-like-cli.git");
        git("-v", "remote", "show", "origin");

        // show help
        git();
        git("help");
        git("help", "git");
        git("help", "add");
        git("help", "remote");
        git("help", "remote", "show");
    }

    private void git(String... args)
    {
        System.out.println("$ git " + Joiner.on(' ').join(args));
        Git.main(args);
        System.out.println();
    }
}
