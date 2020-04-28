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
import sun.misc.Unsafe;

/**
 * Basic thread blocking primitives for creating locks and other
 * synchronization classes.
 * 
 * <p> 用于创建锁和其他同步类的基本线程阻塞原语。
 *
 * <p>This class associates, with each thread that uses it, a permit
 * (in the sense of the {@link java.util.concurrent.Semaphore
 * Semaphore} class). A call to {@code park} will return immediately
 * if the permit is available, consuming it in the process; otherwise
 * it <em>may</em> block.  A call to {@code unpark} makes the permit
 * available, if it was not already available. (Unlike with Semaphores
 * though, permits do not accumulate. There is at most one.)
 * 
 * <p> 此类与使用它的每个线程关联一个许可（就Semaphore类而言）。如果有许可证，将立即返回停车请求，
 * 并在此过程中消耗掉它；否则可能会阻塞。取消停车的调用使许可证可用（如果尚不可用）。 
 * （不过与信号量不同，许可证不会累积。最多只能有一个。）
 *
 * <p>Methods {@code park} and {@code unpark} provide efficient
 * means of blocking and unblocking threads that do not encounter the
 * problems that cause the deprecated methods {@code Thread.suspend}
 * and {@code Thread.resume} to be unusable for such purposes: Races
 * between one thread invoking {@code park} and another thread trying
 * to {@code unpark} it will preserve liveness, due to the
 * permit. Additionally, {@code park} will return if the caller's
 * thread was interrupted, and timeout versions are supported. The
 * {@code park} method may also return at any other time, for "no
 * reason", so in general must be invoked within a loop that rechecks
 * conditions upon return. In this sense {@code park} serves as an
 * optimization of a "busy wait" that does not waste as much time
 * spinning, but must be paired with an {@code unpark} to be
 * effective.
 * 
 * <p> 方法Park和UnPark提供了一种有效的阻塞和解除阻塞线程的方法，这些线程不会遇到导致已弃用的方法
 * Thread.suspend和Thread.resume无法用于以下目的的问题：一个线程在调用Park和试图取消其驻
 * 留的线程之间进行竞争根据许可将保留生命。此外，如果调用者的线程被中断并且支持超时版本，则驻留将返回。 
 * park方法也可能由于“无故”而在其他任何时间返回，因此通常必须在循环中调用该循环，该循环在返回时会重新检查条件。
 * 从这个意义上讲，停车是对“繁忙等待”的优化，它不会浪费太多的时间，而必须与取消停车配对才能有效。
 *
 * <p>The three forms of {@code park} each also support a
 * {@code blocker} object parameter. This object is recorded while
 * the thread is blocked to permit monitoring and diagnostic tools to
 * identify the reasons that threads are blocked. (Such tools may
 * access blockers using method {@link #getBlocker(Thread)}.)
 * The use of these forms rather than the original forms without this
 * parameter is strongly encouraged. The normal argument to supply as
 * a {@code blocker} within a lock implementation is {@code this}.
 * 
 * <p> 停放的三种形式也都支持阻塞对象参数。线程被阻塞时会记录该对象，以允许监视和诊断工具确定线程被阻塞的原因。 
 * （此类工具可以使用getBlocker（Thread）方法访问阻止程序。）强烈建议使用这些形式，而不使用没有此参数的原
 * 始形式。在锁定实现中提供作为阻止程序的正常参数是这个。
 *
 * <p>These methods are designed to be used as tools for creating
 * higher-level synchronization utilities, and are not in themselves
 * useful for most concurrency control applications.  The {@code park}
 * method is designed for use only in constructions of the form:
 * 
 * <p> 这些方法旨在用作创建更高级别的同步实用程序的工具，它们本身对大多数并发控制应用程序没有用。
 * 停放方法仅设计用于以下形式的结构：
 *
 *  <pre> {@code
 * while (!canProceed()) { ... LockSupport.park(this); }}</pre>
 *
 * <p> where neither {@code canProceed} nor any other actions prior to the
 * call to {@code park} entail locking or blocking.  Because only one
 * permit is associated with each thread, any intermediary uses of
 * {@code park} could interfere with its intended effects.
 * 
 * <p> 在进行停放呼叫之前无法继续进行或进行任何其他操作时，都不会锁定或阻塞。因为每个线程仅关联一个许可证，
 * 所以park的任何中间用途都可能会干扰其预期的效果。
 *
 * <p><b>Sample Usage.</b> Here is a sketch of a first-in-first-out
 * non-reentrant lock class:
 * 
 * <p> 样本用法。这是先进先出的不可重入锁类的示意图：
 * 
 *  <pre> {@code
 * class FIFOMutex {
 *   private final AtomicBoolean locked = new AtomicBoolean(false);
 *   private final Queue<Thread> waiters
 *     = new ConcurrentLinkedQueue<Thread>();
 *
 *   public void lock() {
 *     boolean wasInterrupted = false;
 *     Thread current = Thread.currentThread();
 *     waiters.add(current);
 *
 *     // Block while not first in queue or cannot acquire lock
 *     while (waiters.peek() != current ||
 *            !locked.compareAndSet(false, true)) {
 *       LockSupport.park(this);
 *       if (Thread.interrupted()) // ignore interrupts while waiting - 等待时忽略中断
 *         wasInterrupted = true;
 *     }
 *
 *     waiters.remove();
 *     if (wasInterrupted)          // reassert interrupt status on exit - 退出时重新声明中断状态
 *       current.interrupt();
 *   }
 *
 *   public void unlock() {
 *     locked.set(false);
 *     LockSupport.unpark(waiters.peek());
 *   }
 * }}</pre>
 */
public class LockSupport {
    private LockSupport() {} // Cannot be instantiated. - 无法实例化。

    private static void setBlocker(Thread t, Object arg) {
        // Even though volatile, hotspot doesn't need a write barrier here.
    	// 即使不稳定，热点在这里也不需要写障碍。
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * Makes available the permit for the given thread, if it
     * was not already available.  If the thread was blocked on
     * {@code park} then it will unblock.  Otherwise, its next call
     * to {@code park} is guaranteed not to block. This operation
     * is not guaranteed to have any effect at all if the given
     * thread has not been started.
     * 
     * <p> 如果给定线程尚不可用，则使它可用。 如果线程在驻留时被阻止，则它将取消阻止。 否则，
     * 它的下一个停泊呼叫保证不会阻塞。 如果给定线程尚未启动，则不能保证此操作完全无效。
     *
     * @param thread the thread to unpark, or {@code null}, in which case
     *        this operation has no effect
     *        
     * <p> 要取消驻留的线程，或者为null，在这种情况下此操作无效
     */
    public static void unpark(Thread thread) {
        if (thread != null)
            UNSAFE.unpark(thread);
    }

    /**
     * Disables the current thread for thread scheduling purposes unless the
     * permit is available.
     * 
     * <p> 除非有许可，否则出于线程调度目的禁用当前线程。
     *
     * <p>If the permit is available then it is consumed and the call returns
     * immediately; otherwise
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     *
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度的目的，当前线程将被禁用，
     * 并处于休眠状态，直到发生以下三种情况之一：
     * 
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * 
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用者还可以确定例如返回时线程的中断状态。
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     *        
     * <p> 负责此线程停放的同步对象
     * 
     * @since 1.6
     */
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(false, 0L);
        setBlocker(t, null);
    }

    /**
     * Disables the current thread for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     * 
     * <p> 除非允许使用许可，否则在指定的等待时间内禁用当前线程以进行线程调度。
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     * 
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度目的，当前线程将被禁用，
     * 并在发生以下四种情况之一之前处于休眠状态：
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The specified waiting time elapses; or
     * 
     * <p> 经过指定的等待时间； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * 
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the elapsed time
     * upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用方还可以确定例如线程的中断状态或返回时所经过的时间。
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     *        
     * <p> 负责此线程停放的同步对象
     * 
     * @param nanos the maximum number of nanoseconds to wait
     * 
     * <p> 等待的最大纳秒数
     * 
     * @since 1.6
     */
    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    /**
     * Disables the current thread for thread scheduling purposes, until
     * the specified deadline, unless the permit is available.
     * 
     * <p> 除非指定许可，否则禁用当前线程以进行线程调度，直到指定的期限。
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     * 
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度目的，当前线程将被禁用，
     * 并在发生以下四种情况之一之前处于休眠状态：
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The specified deadline passes; or
     * 
     * <p> 指定的期限已过； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the current time
     * upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用者还可以确定例如线程的中断状态或返回时的当前时间。
     *
     * @param blocker the synchronization object responsible for this
     *        thread parking
     * @param deadline the absolute time, in milliseconds from the Epoch,
     *        to wait until
     * @since 1.6
     */
    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    /**
     * Returns the blocker object supplied to the most recent
     * invocation of a park method that has not yet unblocked, or null
     * if not blocked.  The value returned is just a momentary
     * snapshot -- the thread may have since unblocked or blocked on a
     * different blocker object.
     * 
     * <p> 返回提供给尚未取消阻止的park方法的最新调用的阻止程序对象；如果尚未阻止，则返回null。 
     * 返回的值只是一个瞬时快照-线程可能已经解除阻塞或在另一个阻塞对象上被阻塞。
     *
     * @param t the thread
     * @return the blocker - 阻碍者
     * @throws NullPointerException if argument is null
     * @since 1.6
     */
    public static Object getBlocker(Thread t) {
        if (t == null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t, parkBlockerOffset);
    }

    /**
     * Disables the current thread for thread scheduling purposes unless the
     * permit is available.
     * 
     * <p> 除非有许可，否则出于线程调度目的禁用当前线程。
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of three
     * things happens:
     * 
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度的目的，当前线程将被禁用，
     * 并处于休眠状态，直到发生以下三种情况之一：
     *
     * <ul>
     *
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * 
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用者还可以确定例如返回时线程的中断状态。
     * 
     */
    public static void park() {
        UNSAFE.park(false, 0L);
    }

    /**
     * Disables the current thread for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     * 
     * <p> 除非允许使用许可，否则在指定的等待时间内禁用当前线程以进行线程调度。
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     * 
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度目的，当前线程将被禁用，
     * 并在发生以下四种情况之一之前处于休眠状态：
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The specified waiting time elapses; or
     * 
     * <p> 经过指定的等待时间； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * 
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the elapsed time
     * upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用方还可以确定例如线程的中断状态或返回时所经过的时间。
     * 
     *
     * @param nanos the maximum number of nanoseconds to wait
     * 
     * <p> 等待的最大纳秒数
     */
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            UNSAFE.park(false, nanos);
    }

    /**
     * Disables the current thread for thread scheduling purposes, until
     * the specified deadline, unless the permit is available.
     * 
     * <p> 除非指定许可，否则禁用当前线程以进行线程调度，直到指定的期限。
     *
     * <p>If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current thread becomes disabled
     * for thread scheduling purposes and lies dormant until one of four
     * things happens:
     * 
     * <p> 如果许可证可用，则将其消耗掉，并立即返回呼叫； 否则，出于线程调度目的，当前线程将被禁用，
     * 并在发生以下四种情况之一之前处于休眠状态：
     *
     * <ul>
     * <li>Some other thread invokes {@link #unpark unpark} with the
     * current thread as the target; or
     * 
     * <p> 某些其他线程以当前线程为目标调用unpark； 要么
     *
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * 
     * <p> 其他一些线程中断当前线程； 要么
     *
     * <li>The specified deadline passes; or
     * 
     * <p> 指定的期限已过； 要么
     *
     * <li>The call spuriously (that is, for no reason) returns.
     * 
     * <p> 虚假地（即，无故）呼叫返回。
     * </ul>
     *
     * <p>This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the thread to park in the first place. Callers may also determine,
     * for example, the interrupt status of the thread, or the current time
     * upon return.
     * 
     * <p> 此方法不报告其中哪一个导致方法返回。 调用者应重新检查导致线程首先停滞的条件。 
     * 调用者还可以确定例如线程的中断状态或返回时的当前时间。
     *
     * @param deadline the absolute time, in milliseconds from the Epoch,
     *        to wait until
     *        
     * <p> 距纪元的绝对时间（以毫秒为单位）
     */
    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    /**
     * Returns the pseudo-randomly initialized or updated secondary seed.
     * Copied from ThreadLocalRandom due to package access restrictions.
     * 
     * <p> 返回伪随机初始化或更新的辅助种子。 由于程序包访问限制，从ThreadLocalRandom复制。
     */
    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0)
            r = 1; // avoid zero
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    // Hotspot implementation via intrinsics API
    // 通过内在API实现热点
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            parkBlockerOffset = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            SEED = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
