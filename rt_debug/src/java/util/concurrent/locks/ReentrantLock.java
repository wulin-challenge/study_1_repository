/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.Collection;

/**
 * A reentrant mutual exclusion {@link Lock} with the same basic
 * behavior and semantics as the implicit monitor lock accessed using
 * {@code synchronized} methods and statements, but with extended
 * capabilities.
 * 
 * <p> 具有与使用同步方法和语句访问的隐式监视器锁相同的基本行为和语义的可重入互斥锁，但具有扩展功能。
 *
 * <p>A {@code ReentrantLock} is <em>owned</em> by the thread last
 * successfully locking, but not yet unlocking it. A thread invoking
 * {@code lock} will return, successfully acquiring the lock, when
 * the lock is not owned by another thread. The method will return
 * immediately if the current thread already owns the lock. This can
 * be checked using methods {@link #isHeldByCurrentThread}, and {@link
 * #getHoldCount}.
 * 
 * <p> ReentrantLock由最后成功锁定但尚未解锁的线程所拥有。当另一个线程不拥有该锁时，
 * 调用该锁的线程将成功返回该锁。如果当前线程已经拥有该锁，则该方法将立即返回。可以使用
 * isHeldByCurrentThread和getHoldCount方法进行检查。
 *
 * <p>The constructor for this class accepts an optional
 * <em>fairness</em> parameter.  When set {@code true}, under
 * contention, locks favor granting access to the longest-waiting
 * thread.  Otherwise this lock does not guarantee any particular
 * access order.  Programs using fair locks accessed by many threads
 * may display lower overall throughput (i.e., are slower; often much
 * slower) than those using the default setting, but have smaller
 * variances in times to obtain locks and guarantee lack of
 * starvation. Note however, that fairness of locks does not guarantee
 * fairness of thread scheduling. Thus, one of many threads using a
 * fair lock may obtain it multiple times in succession while other
 * active threads are not progressing and not currently holding the
 * lock.
 * Also note that the untimed {@link #tryLock()} method does not
 * honor the fairness setting. It will succeed if the lock
 * is available even if other threads are waiting.
 * 
 * <p> 此类的构造函数接受一个可选的fairness参数。设置为true时，在争用下，锁倾向于授予对等待时间最长的线程的访问。
 * 否则，此锁不能保证任何特定的访问顺序。使用许多线程访问的公平锁定的程序可能会比使用默认设置的程序显示较低的总体吞吐量
 * （即较慢；通常要慢得多），但获得锁定并保证没有饥饿的时间差异较小。但是请注意，锁的公平性不能保证线程调度的公平性。
 * 因此，使用公平锁的多个线程之一可能会连续多次获得它，而其他活动线程没有进行且当前未持有该锁。还要注意，
 * 未定时的tryLock（）方法不支持公平性设置。如果锁定可用，即使其他线程正在等待，它将成功。
 *
 * <p>It is recommended practice to <em>always</em> immediately
 * follow a call to {@code lock} with a {@code try} block, most
 * typically in a before/after construction such as:
 * 
 * <p> 建议的做法是始终立即在调用后使用try块进行锁定，最常见的是在构造之前/之后，例如：
 *
 *  <pre> {@code
 * class X {
 *   private final ReentrantLock lock = new ReentrantLock();
 *   // ...
 *
 *   public void m() {
 *     lock.lock();  // block until condition holds
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock()
 *     }
 *   }
 * }}</pre>
 *
 * <p>In addition to implementing the {@link Lock} interface, this
 * class defines a number of {@code public} and {@code protected}
 * methods for inspecting the state of the lock.  Some of these
 * methods are only useful for instrumentation and monitoring.
 * 
 * <p> 除了实现Lock接口之外，此类还定义了许多用于检查锁状态的公共方法和受保护方法。 
 * 其中一些方法仅对仪器和监视有用。
 *
 * <p>Serialization of this class behaves in the same way as built-in
 * locks: a deserialized lock is in the unlocked state, regardless of
 * its state when serialized.
 * 
 * <p> 此类的序列化与内置锁的行为相同：反序列化的锁处于解锁状态，而不管序列化时的状态如何。
 *
 * <p>This lock supports a maximum of 2147483647 recursive locks by
 * the same thread. Attempts to exceed this limit result in
 * {@link Error} throws from locking methods.
 * 
 * <p> 此锁通过同一线程最多支持2147483647个递归锁。 尝试超过此限制会导致锁定方法引发错误。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    /** 
     * Synchronizer providing all implementation mechanics 
     * 
     * <p> 同步器提供所有实施机制
     */
    private final Sync sync;

    /**
     * Base of synchronization control for this lock. Subclassed
     * into fair and nonfair versions below. Uses AQS state to
     * represent the number of holds on the lock.
     * 
     * <p> 此锁的同步控制基础。 在下面细分为公平和非公平版本。 使用AQS状态表示锁的保留数。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         * 
         * <p> 执行Lock.lock。 子类化的主要原因是允许为非公平版本提供快速路径。
         */
        abstract void lock();

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         * 
         * <p> 执行不公平的tryLock。 tryAcquire是在子类中实现的，
         * 但是都需要对trylock方法进行不公平的尝试。
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
        	
        	// 虽然我们必须在拥有者之前先以一般状态读取状态，但我们不需要这样做就可以检查当前线程是否为拥有者
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // Methods relayed from outer class
        // 从外部类继承的方法

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         * 
         * <p> 从流中重构实例（即反序列化它）。
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state - 重置为解锁状态
        }
    }

    /**
     * Sync object for non-fair locks
     * 
     * <p> 同步对象的非公平锁
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         * 
         * <p> 执行锁定。 尝试立即进行驳船，并在出现故障时备份到正常状态。
         */
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * Sync object for fair locks
     * 
     * <p> 同步对象以获取公平锁
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         * 
         * <p> 公平版本的tryAcquire。 除非递归调用或没有服务员，否则不要授予访问权限。
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * Creates an instance of {@code ReentrantLock}.
     * This is equivalent to using {@code ReentrantLock(false)}.
     * 
     * <p> 创建ReentrantLock的实例。 这等效于使用ReentrantLock（false）。
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * Creates an instance of {@code ReentrantLock} with the
     * given fairness policy.
     * 
     * <p> 使用给定的公平性策略创建ReentrantLock的实例。
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     * 
     * <p> 如果此锁应使用公平的订购策略，则为true
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    /**
     * Acquires the lock.
     * 
     * <p> 获取锁。
     *
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately, setting the lock hold count to one.
     * 
     * <p> 如果没有其他线程持有该锁，则获取该锁并立即返回，将锁保持计数设置为1。
     *
     * <p>If the current thread already holds the lock then the hold
     * count is incremented by one and the method returns immediately.
     * 
     * <p> 如果当前线程已经持有该锁，则持有计数将增加一，该方法将立即返回。
     *
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until the lock has been acquired,
     * at which time the lock hold count is set to one.
     * 
     * <p> 如果锁是由另一个线程持有的，则当前线程将出于线程调度目的而被禁用，并处于休眠状态，直到获取了该锁为止，此时，锁持有计数被设置为1。
     */
    public void lock() {
        sync.lock();
    }

    /**
     * Acquires the lock unless the current thread is
     * {@linkplain Thread#interrupt interrupted}.
     * 
     * <p> 除非当前线程被中断，否则获取锁。
     *
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately, setting the lock hold count to one.
     * 
     * <p> 如果没有其他线程持有该锁，则获取该锁并立即返回，将锁保持计数设置为1。
     *
     * <p>If the current thread already holds this lock then the hold count
     * is incremented by one and the method returns immediately.
     * 
     * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法将立即返回。
     *
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of two things happens:
     * 
     * <p> 如果该锁由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下两种情况之一：
     *
     * <ul>
     *
     * <li>The lock is acquired by the current thread; or
     * 
     * <p> 锁由当前线程获取；要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread.
     * 
     * <p> 其他一些线程中断当前线程。
     *
     * </ul>
     *
     * <p>If the lock is acquired by the current thread then the lock hold
     * count is set to one.
     * 
     * <p> 如果当前线程获取了锁，则锁保持计数将设置为1。
     *
     * <p>If the current thread:
     * 
     * <p> 如果当前线程：
     *
     * <ul>
     *
     * <li>has its interrupted status set on entry to this method; or
     * 
     * <p> 在进入此方法时已设置其中断状态；要么
     *
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring
     * the lock,
     * 
     * <p> 获取锁时中断，
     *
     * </ul>
     *
     * <p> then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * 
     * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
     *
     * <p>In this implementation, as this method is an explicit
     * interruption point, preference is given to responding to the
     * interrupt over normal or reentrant acquisition of the lock.
     * 
     * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁获取。
     *
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     */
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    /**
     * Acquires the lock only if it is not held by another thread at the time
     * of invocation.
     * 
     * <p> 仅当调用时另一个线程未持有该锁时才获取该锁。
     *
     * <p>Acquires the lock if it is not held by another thread and
     * returns immediately with the value {@code true}, setting the
     * lock hold count to one. Even when this lock has been set to use a
     * fair ordering policy, a call to {@code tryLock()} <em>will</em>
     * immediately acquire the lock if it is available, whether or not
     * other threads are currently waiting for the lock.
     * This &quot;barging&quot; behavior can be useful in certain
     * circumstances, even though it breaks fairness. If you want to honor
     * the fairness setting for this lock, then use
     * {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
     * which is almost equivalent (it also detects interruption).
     * 
     * <p> 如果没有其他线程持有该锁，则获取该锁，并立即返回true值，将锁保持计数设置为1。 
     * 即使已将此锁设置为使用公平的排序策略，对tryLock（）的调用也会立即获取该锁（如果有），
     * 无论当前是否有其他线程在等待该锁。 即使破坏公平性，这种“讨价还价”的行为在某些情况下还是有用的。 
     * 如果要遵守此锁的公平性设置，请使用几乎等效的tryLock（0，TimeUnit.SECONDS）
     * （它还会检测到中断）。
     *
     * <p>If the current thread already holds this lock then the hold
     * count is incremented by one and the method returns {@code true}.
     * 
     * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法返回true。
     *
     * <p>If the lock is held by another thread then this method will return
     * immediately with the value {@code false}.
     * 
     * <p> 如果锁由另一个线程持有，则此方法将立即返回false值。
     *
     * @return {@code true} if the lock was free and was acquired by the
     *         current thread, or the lock was already held by the current
     *         thread; and {@code false} otherwise
     *         
     * <p> 如果锁是空闲的并且由当前线程获取，或者锁已由当前线程持有，则返回true；否则为true。 否则为假
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    /**
     * Acquires the lock if it is not held by another thread within the given
     * waiting time and the current thread has not been
     * {@linkplain Thread#interrupt interrupted}.
     * 
     * <p> 如果在给定的等待时间内另一个线程未持有该锁并且当前线程尚未中断，则获取该锁。
     *
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately with the value {@code true}, setting the lock hold count
     * to one. If this lock has been set to use a fair ordering policy then
     * an available lock <em>will not</em> be acquired if any other threads
     * are waiting for the lock. This is in contrast to the {@link #tryLock()}
     * method. If you want a timed {@code tryLock} that does permit barging on
     * a fair lock then combine the timed and un-timed forms together:
     * 
     * <p> 如果没有其他线程持有该锁，则获取该锁，并立即返回true值，将锁保持计数设置为1。 
     * 如果将此锁设置为使用公平的排序策略，那么如果有任何其他线程在等待该锁，则不会获取可用锁。 
     * 这与tryLock（）方法相反。 如果您想要一个定时tryLock确实允许在公平锁上插入，
     * 则将定时和非定时形式组合在一起：
     *
     *  <pre> {@code
     * if (lock.tryLock() ||
     *     lock.tryLock(timeout, unit)) {
     *   ...
     * }}</pre>
     *
     * <p>If the current thread
     * already holds this lock then the hold count is incremented by one and
     * the method returns {@code true}.
     * 
     * <p> 如果当前线程已经持有此锁，则持有计数将增加一，并且该方法返回true。
     *
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     * 
     * <p> 如果该锁由另一个线程持有，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，直到发生以下三种情况之一：
     *
     * <ul>
     *
     * <li>The lock is acquired by the current thread; or
     * 
     * <p> 锁由当前线程获取；要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程；要么
     *
     * <li>The specified waiting time elapses
     * 
     * <p> 经过指定的等待时间
     *
     * </ul>
     *
     * <p>If the lock is acquired then the value {@code true} is returned and
     * the lock hold count is set to one.
     * 
     * <p> 如果获取了锁，则返回true值，并将锁保持计数设置为1。
     *
     * <p>If the current thread:
     * 
     * <p> 如果当前线程：
     *
     * <ul>
     *
     * <li>has its interrupted status set on entry to this method; or
     * 
     * <p> 在进入此方法时已设置其中断状态；要么
     *
     * <li>is {@linkplain Thread#interrupt interrupted} while
     * acquiring the lock,
     * 
     * <p> 获取锁时中断，
     *
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * 
     * <p> 然后抛出InterruptedException并清除当前线程的中断状态。
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     * 
     * <p> 如果经过了指定的等待时间，则返回值false。如果时间小于或等于零，则该方法将根本不等待。
     *
     * <p>In this implementation, as this method is an explicit
     * interruption point, preference is given to responding to the
     * interrupt over normal or reentrant acquisition of the lock, and
     * over reporting the elapse of the waiting time.
     * 
     * <p> 在此实现中，由于此方法是显式的中断点，因此优先于对中断的响应而不是正常或可重入的锁定获取，
     * 而是优先报告等待时间的流逝。
     *
     * @param timeout the time to wait for the lock
     * 
     * <p> 等待锁的时间
     * 
     * @param unit the time unit of the timeout argument
     * 
     * <p> 超时参数的时间单位
     * 
     * @return {@code true} if the lock was free and was acquired by the
     *         current thread, or the lock was already held by the current
     *         thread; and {@code false} if the waiting time elapsed before
     *         the lock could be acquired
     *         
     * <p> 如果锁是空闲的并且由当前线程获取，或者锁已由当前线程持有，则返回true；否则为true。 
     * 如果可以获取锁之前的等待时间已过，则返回false
     * 
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     * 
     * @throws NullPointerException if the time unit is null
     * 
     * <p> 如果时间单位为null
     */
    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    /**
     * Attempts to release this lock.
     * 
     * <p> 尝试释放此锁。
     *
     * <p>If the current thread is the holder of this lock then the hold
     * count is decremented.  If the hold count is now zero then the lock
     * is released.  If the current thread is not the holder of this
     * lock then {@link IllegalMonitorStateException} is thrown.
     * 
     * <p> 如果当前线程是此锁的持有者，则保留计数将减少。 如果保持计数现在为零，则释放锁定。
     *  如果当前线程不是此锁的持有者，则抛出IllegalMonitorStateException。
     *
     * @throws IllegalMonitorStateException if the current thread does not
     *         hold this lock
     *         
     * <p> 如果当前线程不持有此锁
     */
    public void unlock() {
        sync.release(1);
    }

    /**
     * Returns a {@link Condition} instance for use with this
     * {@link Lock} instance.
     * 
     * <p> 返回用于此Lock实例的Condition实例。
     *
     * <p>The returned {@link Condition} instance supports the same
     * usages as do the {@link Object} monitor methods ({@link
     * Object#wait() wait}, {@link Object#notify notify}, and {@link
     * Object#notifyAll notifyAll}) when used with the built-in
     * monitor lock.
     * 
     * <p> 当与内置监视器锁定一起使用时，返回的Condition实例支持与Object监视器方法
     * （wait，notify和notifyAll）相同的用法。
     *
     * <ul>
     *
     * <li>If this lock is not held when any of the {@link Condition}
     * {@linkplain Condition#await() waiting} or {@linkplain
     * Condition#signal signalling} methods are called, then an {@link
     * IllegalMonitorStateException} is thrown.
     * 
     * <p> 如果在调用任何条件等待或信令方法时未持有此锁，则将抛出IllegalMonitorStateException。
     *
     * <li>When the condition {@linkplain Condition#await() waiting}
     * methods are called the lock is released and, before they
     * return, the lock is reacquired and the lock hold count restored
     * to what it was when the method was called.
     * 
     * <p> 调用条件等待方法时，将释放锁，并在它们返回之前，重新获取锁，并将锁保持计数恢复为调用该方法时的计数。
     *
     * <li>If a thread is {@linkplain Thread#interrupt interrupted}
     * while waiting then the wait will terminate, an {@link
     * InterruptedException} will be thrown, and the thread's
     * interrupted status will be cleared.
     * 
     * <p> 如果线程在等待时被中断，则等待将终止，将抛出InterruptedException，并清除线程的中断状态。
     *
     * <li> Waiting threads are signalled in FIFO order.
     * 
     * <p> 等待的线程以FIFO顺序发出信号。
     *
     * <li>The ordering of lock reacquisition for threads returning
     * from waiting methods is the same as for threads initially
     * acquiring the lock, which is in the default case not specified,
     * but for <em>fair</em> locks favors those threads that have been
     * waiting the longest.
     * 
     * <p> 从等待方法返回的线程的锁重新获取顺序与最初获取锁的线程相同（默认情况下未指定），
     * 但对于公平锁，优先使用等待时间最长的线程。
     *
     * </ul>
     *
     * @return the Condition object - 条件对象
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
     * Queries the number of holds on this lock by the current thread.
     * 
     * <p> 查询当前线程对该锁的保持次数。
     *
     * <p>A thread has a hold on a lock for each lock action that is not
     * matched by an unlock action.
     * 
     * <p> 对于每个未与解锁动作匹配的锁定动作，线程都会拥有一个锁。
     *
     * <p>The hold count information is typically only used for testing and
     * debugging purposes. For example, if a certain section of code should
     * not be entered with the lock already held then we can assert that
     * fact:
     *
     * <p> 保留计数信息通常仅用于测试和调试目的。 例如，如果不应该使用已经持有的锁来输入特定的代码段，
     * 那么我们可以断言以下事实：
     * 
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *   public void m() {
     *     assert lock.getHoldCount() == 0;
     *     lock.lock();
     *     try {
     *       // ... method body
     *     } finally {
     *       lock.unlock();
     *     }
     *   }
     * }}</pre>
     *
     * @return the number of holds on this lock by the current thread,
     *         or zero if this lock is not held by the current thread
     *         
     * <p> 当前线程对该锁的保持次数；如果当前线程未保持此锁，则为零
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * Queries if this lock is held by the current thread.
     * 
     * <p> 查询此锁是否由当前线程持有。
     *
     * <p>Analogous to the {@link Thread#holdsLock(Object)} method for
     * built-in monitor locks, this method is typically used for
     * debugging and testing. For example, a method that should only be
     * called while a lock is held can assert that this is the case:
     * 
     * <p> 与内置监视器锁的Thread.holdsLock（Object）方法类似，此方法通常用于调试和测试。 
     * 例如，仅在持有锁的情况下才应调用的方法可以断言是这种情况：
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert lock.isHeldByCurrentThread();
     *       // ... method body
     *   }
     * }}</pre>
     *
     * <p>It can also be used to ensure that a reentrant lock is used
     * in a non-reentrant manner, for example:
     * 
     * <p> 它还可以用于确保以非可重入方式使用可重入锁，例如：
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert !lock.isHeldByCurrentThread();
     *       lock.lock();
     *       try {
     *           // ... method body
     *       } finally {
     *           lock.unlock();
     *       }
     *   }
     * }}</pre>
     *
     * @return {@code true} if current thread holds this lock and
     *         {@code false} otherwise
     *         
     * <p> 如果当前线程持有此锁，则返回true，否则返回false
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * Queries if this lock is held by any thread. This method is
     * designed for use in monitoring of the system state,
     * not for synchronization control.
     * 
     * <p> 查询此锁是否由任何线程持有。 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @return {@code true} if any thread holds this lock and
     *         {@code false} otherwise
     *         
     * <p> 如果任何线程持有此锁，则为true，否则为false
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
     * Returns {@code true} if this lock has fairness set true.
     * 
     * <p> 如果此锁的公平性设置为true，则返回true。
     *
     * @return {@code true} if this lock has fairness set true
     * 
     * <p> 如果此锁的公平性设置为true，则为true
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * Returns the thread that currently owns this lock, or
     * {@code null} if not owned. When this method is called by a
     * thread that is not the owner, the return value reflects a
     * best-effort approximation of current lock status. For example,
     * the owner may be momentarily {@code null} even if there are
     * threads trying to acquire the lock but have not yet done so.
     * This method is designed to facilitate construction of
     * subclasses that provide more extensive lock monitoring
     * facilities.
     * 
     * <p> 返回当前拥有此锁的线程；如果不拥有，则返回null。 当非所有者的线程调用此方法时，
     * 返回值反映当前锁定状态的尽力而为近似。 例如，即使有线程尝试获取锁，但所有者尚未拥有，
     * 所有者可能暂时为null。 设计此方法是为了便于构造提供更广泛的锁监视功能的子类。
     *
     * @return the owner, or {@code null} if not owned
     * 
     * <p> 所有者；如果不拥有，则为null
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * Queries whether any threads are waiting to acquire this lock. Note that
     * because cancellations may occur at any time, a {@code true}
     * return does not guarantee that any other thread will ever
     * acquire this lock.  This method is designed primarily for use in
     * monitoring of the system state.
     * 
     * <p> 查询是否有任何线程正在等待获取此锁。 请注意，由于取消可能随时发生，
     * 因此返回true不能保证任何其他线程都会获得此锁。 此方法主要设计用于监视系统状态。
     *
     * @return {@code true} if there may be other threads waiting to
     *         acquire the lock
     *         
     * <p> 如果可能还有其他线程在等待获取锁，则返回true
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire this
     * lock. Note that because cancellations may occur at any time, a
     * {@code true} return does not guarantee that this thread
     * will ever acquire this lock.  This method is designed primarily for use
     * in monitoring of the system state.
     * 
     * <p> 查询给定线程是否正在等待获取此锁。 请注意，由于取消可能随时发生，
     * 因此返回true不能保证此线程将获得此锁。 此方法主要设计用于监视系统状态。
     *
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * 
     * <p> 如果给定线程排队等待此锁，则返回true
     * 
     * @throws NullPointerException if the thread is null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire this lock.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring of the system state, not for synchronization
     * control.
     * 
     * <p> 返回等待获取此锁的线程数的估计值。 该值只是一个估计值，因为在此方法遍历内部数据结构时，
     * 线程数可能会动态变化。 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @return the estimated number of threads waiting for this lock
     * 
     * <p> 等待此锁的估计线程数
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire this lock.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     * 
     * <p> 返回一个包含可能正在等待获取此锁的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 设计此方法是为了便于构造子类，
     * 以提供更广泛的监视功能。
     *
     * @return the collection of threads - 线程集合
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     * 
     * <p> 查询是否有任何线程正在等待与此锁关联的给定条件。 请注意，因为超时和中断可能随时发生，
     * 所以真正的返回并不保证将来的信号会唤醒任何线程。 此方法主要设计用于监视系统状态。
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * 
     * <p> 如果有任何等待线程，则返回true
     * 
     * @throws IllegalMonitorStateException if this lock is not held
     * 
     * <p> 如果未持有此锁
     * 
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联
     * 
     * @throws NullPointerException if the condition is null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     * 
     * <p> 返回等待与此锁关联的给定条件的线程数的估计值。 请注意，由于超时和中断可能随时发生，
     * 因此估算值仅用作实际侍者数的上限。 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * 
     * <p> 估计的等待线程数
     * 
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联
     * 
     * @throws NullPointerException if the condition is null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this lock.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a
     * best-effort estimate. The elements of the returned collection
     * are in no particular order.  This method is designed to
     * facilitate construction of subclasses that provide more
     * extensive condition monitoring facilities.
     * 
     * 
     * <p> 返回一个包含那些可能正在等待与此锁相关的给定条件的线程的集合。 因为实际的线程
     * 集在构造此结果时可能会动态变化，所以返回的集合只是尽力而为的估计。 返回的集合的元素
     * 没有特定的顺序。 设计此方法是为了便于构造提供更广泛的状态监视工具的子类。
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if this lock is not held
     * 
     * 
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     *         
     * <p> 如果给定条件与此锁没有关联 
     * 
     * @throws NullPointerException if the condition is null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Returns a string identifying this lock, as well as its lock state.
     * The state, in brackets, includes either the String {@code "Unlocked"}
     * or the String {@code "Locked by"} followed by the
     * {@linkplain Thread#getName name} of the owning thread.
     * 
     * <p> 返回标识此锁定及其锁定状态的字符串。 括号中的状态包括字符串“ Unlocked”或字符串“ Locked by”，后跟拥有线程的名称。
     *
     * @return a string identifying this lock, as well as its lock state
     * 
     * <p> 标识此锁及其锁状态的字符串
     */
    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
}
