package shim.net.minecraft.util;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.forge.EmiAgnosForge;
import dev.emi.emi.runtime.EmiLog;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    private static final ExecutorService IO_WORKER_EXECUTOR = createIoWorker("IO-Worker-", false);

    public static ExecutorService getIoWorkerExecutor() {
        return IO_WORKER_EXECUTOR;
    }

    private static ExecutorService createIoWorker(String namePrefix, boolean daemon) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return Executors.newCachedThreadPool((runnable) -> {
            Thread thread = new Thread(runnable);
            thread.setName(namePrefix + atomicInteger.getAndIncrement());
            thread.setDaemon(daemon);
            thread.setUncaughtExceptionHandler(Utils::uncaughtExceptionHandler);
            return thread;
        });
    }

    private static void uncaughtExceptionHandler(Thread thread, Throwable t) {
        throwOrPause(t);
        if (t instanceof CompletionException) {
            t = t.getCause();
        }

        if (t instanceof CrashException crashException) {
            System.out.println(crashException.getReport().getDescription());
            System.exit(-1);
        }

        EmiLog.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), t);
    }

    public static <T extends Throwable> T throwOrPause(T t) {
        if (EmiAgnosForge.isDevelopmentEnvironment()) {
            EmiLog.error("Trying to throw a fatal exception, pausing in IDE", t);
            pause(t.getMessage());
        }

        return t;
    }

    private static void pause(String message) {
        Instant instant = Instant.now();
        EmiLog.warn("Did you remember to set a breakpoint here?");
        boolean bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            EmiLog.warn(message);
        }

    }

    public static void error(String message) {
        EmiLog.error(message);
        if (EmiAgnos.isDevelopmentEnvironment()) {
            pause(message);
        }

    }

    public static void error(String message, Throwable throwable) {
        EmiLog.error(message, throwable);
        if (EmiAgnos.isDevelopmentEnvironment()) {
            pause(message);
        }

    }
}
