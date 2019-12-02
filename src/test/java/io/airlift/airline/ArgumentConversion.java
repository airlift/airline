package io.airlift.airline;

class ArgumentConversion
{
    private ArgumentConversion() {}

    public static void main(String... args)
    {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("argtest")
                .withDescription("test the argument converter")
                .withDefaultCommand(ArgumentConversions.class);

        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }

    @Command(name = "argumentconversion")
    public static class ArgumentConversions
            implements Runnable
    {
        @Option(type = OptionType.GLOBAL, name = "--constructor", description = "String of max. 5 chars")
        public ArgumentConstructor constructedArg;

        @Option(type = OptionType.GLOBAL, name = "--from-string", description = "String of max. 5 chars")
        public ArgumentFromString fromStringArg;

        @Option(type = OptionType.GLOBAL, name = "--value-of", description = "String of max. 5 chars")
        public ArgumentValueOf valueOfArg;

        @Override
        public void run()
        {
            System.out.println("ConstructedArg: " + constructedArg);
            System.out.println("FromStringArg: " + fromStringArg);
            System.out.println("ValueOfArg: " + valueOfArg);
        }
    }

    public static class ArgumentConstructor
    {
        private String value;

        public ArgumentConstructor(String argument) throws IllegalArgumentException
        {
            if (argument.length() > 5) {
                throw new IllegalArgumentException("(1) Argument too long.");
            }

            this.value = argument;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    public static class ArgumentValueOf
    {
        private String value;

        public static ArgumentValueOf valueOf(String str)
        {
            return new ArgumentValueOf(str);
        }

        private ArgumentValueOf(String argument) throws IllegalArgumentException
        {
            if (argument.length() > 5) {
                throw new IllegalArgumentException("(2) Argument too long.");
            }

            this.value = argument;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    public static class ArgumentFromString
    {
        private String value;

        public static ArgumentFromString fromString(String str)
        {
            return new ArgumentFromString(str);
        }

        private ArgumentFromString(String argument) throws IllegalArgumentException
        {
            if (argument.length() > 5) {
                throw new IllegalArgumentException("(3) Argument too long.");
            }

            this.value = argument;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }
}
