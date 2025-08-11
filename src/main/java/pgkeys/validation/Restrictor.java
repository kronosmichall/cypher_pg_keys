package pgkeys.validation;

enum Restrictor {
    IDENTIFIER,
    EXCLUSIVE,
    EXCLUSIVE_MANDATORY,
    EXCLUSIVE_SINGLETON,
    MANDATORY,
    SINGLETON;

    static final String regex = "(IDENTIFIER|EXCLUSIVE|EXCLUSIVE MANDATORY|EXCLUSIVE SINGLETON|MANDATORY|SINGLETON)";

    static Restrictor fromString(String str) {
        switch (str) {
            case "IDENTIFIER" -> {
                return IDENTIFIER;
            }
            case "EXCLUSIVE MANDATORY" -> {
                return EXCLUSIVE_MANDATORY;
            }
            case "EXCLUSIVE SINGLETON" -> {
                return EXCLUSIVE_SINGLETON;
            }
            case "EXCLUSIVE" -> {
                return EXCLUSIVE;
            }
            case "MANDATORY" -> {
                return MANDATORY;
            }
            case "SINGLETON" -> {
                return SINGLETON;
            }
            default -> throw new IllegalArgumentException("Unknown restrictor: %s".formatted(str));
        }
    }
}
