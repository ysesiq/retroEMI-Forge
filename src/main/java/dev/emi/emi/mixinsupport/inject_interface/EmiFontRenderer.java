package dev.emi.emi.mixinsupport.inject_interface;

public interface EmiFontRenderer {
    default int setTextColor(int color) {
        return 0;
    }
}
