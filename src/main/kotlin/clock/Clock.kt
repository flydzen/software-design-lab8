package clock

import java.time.Instant

interface Clock {
    fun now(): Instant;
}

class NormalClock: Clock {
    override fun now(): Instant = Instant.now()
}

class SetableClock(private var instant: Instant): Clock {
    override fun now(): Instant = instant
    fun setNow(now: Instant) {
        instant = now
    }
}