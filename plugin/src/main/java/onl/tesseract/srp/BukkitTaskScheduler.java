package onl.tesseract.srp;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BukkitTaskScheduler implements TaskScheduler {

    private final Plugin plugin;
    private final BukkitScheduler bukkitScheduler;

    public BukkitTaskScheduler(Plugin plugin, BukkitScheduler bukkitScheduler) {
        this.plugin = plugin;
        this.bukkitScheduler = bukkitScheduler;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        BukkitTask taskObj = bukkitScheduler.runTaskTimer(plugin, task, 0L, period.getSeconds() * 20);
        return new BukkitScheduledTimerFuture(taskObj, Instant.now(), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static class BukkitScheduledTimerFuture implements ScheduledFuture<Object> {
        private final BukkitTask task;
        private final Instant startTime;
        private final Duration period;

        public BukkitScheduledTimerFuture(BukkitTask task, Instant startTime, Duration period) {
            this.task = task;
            this.startTime = startTime;
            this.period = period;
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == null) return 1;
            return Long.compare(getDelay(TimeUnit.SECONDS), other.getDelay(TimeUnit.SECONDS));
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long nextIn = (Instant.now().getEpochSecond() - startTime.toEpochMilli()) % period.toMillis();
            return Duration.ofMillis(nextIn).get(unit.toChronoUnit());
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            task.cancel();
            return true;
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}

