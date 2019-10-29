package utils

import groovy.util.logging.Slf4j
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.FailsafeExecutor
import net.jodah.failsafe.RetryPolicy
import net.jodah.failsafe.function.CheckedRunnable
import net.jodah.failsafe.function.CheckedSupplier

import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@Singleton
@Slf4j
class AppFailsafe {
    RetryPolicy retryPolicy = new RetryPolicy()
            .withBackoff(2, 20, ChronoUnit.SECONDS)
            .withMaxRetries(-1)
            .onRetry { e ->
                log.warn("Failure. Retrying.... " + e)
            }

    @Delegate
    FailsafeExecutor failsafe = Failsafe.with(retryPolicy)

    void run(Closure closure) {
        failsafe.run(closure as CheckedRunnable)
    }

    public CompletableFuture<Void> runAsync(Closure closure) {
        return failsafe.runAsync(closure as CheckedRunnable)
    }

    public <T> T get(Closure closure) {
        return failsafe.get(closure as CheckedSupplier<T>) as T
    }

    public <T> CompletableFuture<T> getAsync(Closure closure) {
        return failsafe.getStageAsync(closure as CheckedSupplier<? extends CompletionStage<T>>)
    }
}
