module jpackagefx.main {
    requires transitive javafx.controls;
    requires com.dlsc.jpackage.module1;
    requires com.dlsc.jpackage.module2;

    exports com.dlsc.jpackagefx;
}