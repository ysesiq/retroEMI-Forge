package dev.emi.emi.mixinsupport.inject_interface;

public interface EmiGuiContainer {

    default int setXSize(int s) { return 0; }

    default int setYSize(int s) { return 0; }
}
