package org.iq80.cli;

import org.testng.annotations.Test;

public class GitTest
{
    @Test
    public void test()
    {
        // show help
        Git.main(new String[]{});
        Git.main(new String[]{"help"});
        Git.main(new String[]{"help", "git"});
        Git.main(new String[]{"help", "add"});
        Git.main(new String[]{"help", "remote"});
        Git.main(new String[]{"help", "remote", "show"});

        // simple command parsing example
        Git.main(new String[]{"add", "file"});
        Git.main(new String[]{"remote", "add", "origin", "git@github.com:dain/git-like-cli.git"});
        Git.main(new String[]{"remote", "show", "origin"});
    }
}
