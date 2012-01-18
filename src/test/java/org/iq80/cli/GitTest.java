package org.iq80.cli;

import org.testng.annotations.Test;

public class GitTest
{
    @Test
    public void test()
    {
        Git.main(new String[]{"add", "file"});
        Git.main(new String[]{"remote", "add", "origin", "git@github.com:dain/git-like-cli.git"});
        Git.main(new String[]{ "remote", "show", "origin"});
    }
}
