package top.mrxiaom.sweetmail.config.entry;

import top.mrxiaom.sweetmail.utils.Util;

public class MailVariable {
    public enum Type {
        STRING, NUMBER, INTEGER
        ;
        public boolean isValid(String s) {
            if (equals(STRING)) return true;
            if (equals(NUMBER)) {
                return Util.parseDouble(s).isPresent();
            }
            if (equals(INTEGER)) {
                return Util.parseInt(s).isPresent();
            }
            return false;
        }
    }
    public final Type type;
    public final String name;
    public final String defaultValue;

    public MailVariable(Type type, String name, String defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
    }
}
