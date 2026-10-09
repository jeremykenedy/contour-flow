package com.jeremykenedy.contourflow;

public final class ContourFlowOptionsTest {
    public static void main(String[] args) {
        defaultsAreBalanced();
        valuesStayWithinTheirRanges();
        randomChoicesResolveOnceAndStayInRange();
        System.out.println("Contour Flow option tests passed");
    }

    private static void defaultsAreBalanced() {
        ContourFlowOptions options = ContourFlowOptions.defaults();
        require(options.palette == 0 && options.relief == 0, "Default palette and relief");
        require(
                options.density == 3 && options.speed == 3 && options.weight == 2,
                "Default motion");
        require(options.lighting == 0 && options.brightness == 3, "Default lighting");
        require(options.randomMask == 0, "Defaults are stable");
    }

    private static void valuesStayWithinTheirRanges() {
        ContourFlowOptions options = new ContourFlowOptions(-1, 8, 0, 9, -2, 7, 8, 255);
        require(options.palette == 0 && options.relief == 2, "Choice bounds");
        require(options.density == 1 && options.speed == 5 && options.weight == 1, "Range bounds");
        require(options.lighting == 2 && options.brightness == 5, "Display bounds");
        require(options.randomMask == 127, "Only implemented settings can randomize");
    }

    private static void randomChoicesResolveOnceAndStayInRange() {
        ContourFlowOptions all = new ContourFlowOptions(0, 0, 1, 1, 1, 0, 1, 127);
        ContourFlowOptions resolved = all.resolve(421);
        require(resolved.randomMask == 0, "Choices resolve when a scene starts");
        require(resolved.palette >= 0 && resolved.palette < 4, "Palette range");
        require(resolved.relief >= 0 && resolved.relief < 3, "Relief range");
        require(resolved.density >= 1 && resolved.density <= 5, "Density range");
        require(resolved.speed >= 1 && resolved.speed <= 5, "Speed range");
        require(resolved.weight >= 1 && resolved.weight <= 5, "Weight range");
        require(resolved.lighting >= 0 && resolved.lighting < 3, "Lighting range");
        require(resolved.brightness >= 1 && resolved.brightness <= 5, "Brightness range");
        require(resolved.palette == all.resolve(421).palette, "Resolution is stable for a seed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
