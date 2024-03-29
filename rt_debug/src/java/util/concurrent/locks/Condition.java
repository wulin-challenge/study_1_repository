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
import java.util.Date;

/**
 * {@code Condition} factors out the {@code Object} monitor
 * methods ({@link Object#wait() wait}, {@link Object#notify notify}
 * and {@link Object#notifyAll notifyAll}) into distinct objects to
 * give the effect of having multiple wait-sets per object, by
 * combining them with the use of arbitrary {@link Lock} implementations.
 * Where a {@code Lock} replaces the use of {@code synchronized} methods
 * and statements, a {@code Condition} replaces the use of the Object
 * monitor methods.
 * 
 * <p> 条件将对象监视方法（wait，notify和notifyAll）分解为不同的对象，从而通过与任意
 * Lock实现结合使用，从而使每个对象具有多个等待集。如果Lock替换了同步方法和语句的使用，而
 * Condition替换了Object监视器方法的使用。
 *
 * <p>Conditions (also known as <em>condition queues</em> or
 * <em>condition variables</em>) provide a means for one thread to
 * suspend execution (to &quot;wait&quot;) until notified by another
 * thread that some state condition may now be true.  Because access
 * to this shared state information occurs in different threads, it
 * must be protected, so a lock of some form is associated with the
 * condition. The key property that waiting for a condition provides
 * is that it <em>atomically</em> releases the associated lock and
 * suspends the current thread, just like {@code Object.wait}.
 * 
 * <p> 条件（也称为条件队列或条件变量）为一个线程暂停执行（“等待”）直到另一线程通知某些状态条件
 * 现在可能为真提供了一种方法。由于对该共享状态信息的访问发生在不同的线程中，因此必须对其进行保护，
 * 因此某种形式的锁与该条件相关联。等待条件提供的关键属性是，它自动释放关联的锁并挂起当前线程，
 * 就像Object.wait一样。
 *
 * <p>A {@code Condition} instance is intrinsically bound to a lock.
 * To obtain a {@code Condition} instance for a particular {@link Lock}
 * instance use its {@link Lock#newCondition newCondition()} method.
 * 
 * <p> Condition实例从本质上绑定到锁。要获取特定Lock实例的Condition实例，请使用其
 * newCondition（）方法。
 *
 * <p>As an example, suppose we have a bounded buffer which supports
 * {@code put} and {@code take} methods.  If a
 * {@code take} is attempted on an empty buffer, then the thread will block
 * until an item becomes available; if a {@code put} is attempted on a
 * full buffer, then the thread will block until a space becomes available.
 * We would like to keep waiting {@code put} threads and {@code take}
 * threads in separate wait-sets so that we can use the optimization of
 * only notifying a single thread at a time when items or spaces become
 * available in the buffer. This can be achieved using two
 * {@link Condition} instances.
 * 
 * <p> 例如，假设我们有一个支持put和take方法的有限缓冲区。如果尝试在空缓冲区上进行取带，则线程将阻塞，
 * 直到有可用项为止。如果尝试在完整的缓冲区上进行放置，则线程将阻塞，直到有可用空间为止。我们希望继续等待放置线程，
 * 并在单独的等待集中获取线程，以便我们可以使用仅当缓冲区中的项目或空间可用时才通知单个线程的优化。这可以使用两个
 * Condition实例来实现。
 * <pre>
 * class BoundedBuffer {
 *   <b>final Lock lock = new ReentrantLock();</b>
 *   final Condition notFull  = <b>lock.newCondition(); </b>
 *   final Condition notEmpty = <b>lock.newCondition(); </b>
 *
 *   final Object[] items = new Object[100];
 *   int putptr, takeptr, count;
 *
 *   public void put(Object x) throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == items.length)
 *         <b>notFull.await();</b>
 *       items[putptr] = x;
 *       if (++putptr == items.length) putptr = 0;
 *       ++count;
 *       <b>notEmpty.signal();</b>
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 *
 *   public Object take() throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == 0)
 *         <b>notEmpty.await();</b>
 *       Object x = items[takeptr];
 *       if (++takeptr == items.length) takeptr = 0;
 *       --count;
 *       <b>notFull.signal();</b>
 *       return x;
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 * }
 * </pre>
 *
 * <p> (The {@link java.util.concurrent.ArrayBlockingQueue} class provides
 * this functionality, so there is no reason to implement this
 * sample usage class.)
 * 
 * <p> （java.util.concurrent.ArrayBlockingQueue类提供了此功能，因此没有理由实现此示例用法类。）
 *
 * <p>A {@code Condition} implementation can provide behavior and semantics
 * that is
 * different from that of the {@code Object} monitor methods, such as
 * guaranteed ordering for notifications, or not requiring a lock to be held
 * when performing notifications.
 * If an implementation provides such specialized semantics then the
 * implementation must document those semantics.
 * 
 * <p> 条件实现可以提供与对象监视方法不同的行为和语义，例如保证通知的顺序，或者在执行通知时不需要保持锁定。
 * 如果实现提供了这种专门的语义，则实现必须记录这些语义。
 *
 * <p>Note that {@code Condition} instances are just normal objects and can
 * themselves be used as the target in a {@code synchronized} statement,
 * and can have their own monitor {@link Object#wait wait} and
 * {@link Object#notify notification} methods invoked.
 * Acquiring the monitor lock of a {@code Condition} instance, or using its
 * monitor methods, has no specified relationship with acquiring the
 * {@link Lock} associated with that {@code Condition} or the use of its
 * {@linkplain #await waiting} and {@linkplain #signal signalling} methods.
 * It is recommended that to avoid confusion you never use {@code Condition}
 * instances in this way, except perhaps within their own implementation.
 * 
 * <p> 请注意，Condition实例只是普通对象，它们本身可以用作同步语句中的目标，并且可以调用自己的监视器等待和通知方法。
 * 获取条件实例的监视器锁或使用​​其监视器方法与获取与该条件相关联的锁或使用其等待和信令方法没有特定的关系。建议避免混淆，
 * 除非可能在自己的实现中，否则不要以这种方式使用Condition实例。
 *
 * <p>Except where noted, passing a {@code null} value for any parameter
 * will result in a {@link NullPointerException} being thrown.
 * 
 * <p> 除非另有说明，否则为任何参数传递null值都会导致引发NullPointerException。
 *
 * <h3>Implementation Considerations</h3>
 * 
 * <p> 实施注意事项
 *
 * <p>When waiting upon a {@code Condition}, a &quot;<em>spurious
 * wakeup</em>&quot; is permitted to occur, in
 * general, as a concession to the underlying platform semantics.
 * This has little practical impact on most application programs as a
 * {@code Condition} should always be waited upon in a loop, testing
 * the state predicate that is being waited for.  An implementation is
 * free to remove the possibility of spurious wakeups but it is
 * recommended that applications programmers always assume that they can
 * occur and so always wait in a loop.
 * 
 * <p> 当等待条件时，通常会允许“虚假唤醒”，作为对底层平台语义的让步。这对大多数应用程序几乎没有实际影响，
 * 因为应该始终在循环中等待条件，测试正在等待的状态谓词。一个实现可以自由地消除虚假唤醒的可能性，
 * 但是建议应用程序程序员始终假定它们会发生，因此总是在循环中等待。
 *
 * <p>The three forms of condition waiting
 * (interruptible, non-interruptible, and timed) may differ in their ease of
 * implementation on some platforms and in their performance characteristics.
 * In particular, it may be difficult to provide these features and maintain
 * specific semantics such as ordering guarantees.
 * Further, the ability to interrupt the actual suspension of the thread may
 * not always be feasible to implement on all platforms.
 * 
 * <p> 条件等待的三种形式（可中断，不可中断和定时）在它们在某些平台上的实现容易程度和性能特征上可能有所不同。
 * 特别是，可能很难提供这些功能并维护特定的语义，例如排序保证。此外，中断线程的实际挂起的能力可能并不总是在所有平
 * 台上都可行。
 *
 * <p>Consequently, an implementation is not required to define exactly the
 * same guarantees or semantics for all three forms of waiting, nor is it
 * required to support interruption of the actual suspension of the thread.
 * 
 * <p> 因此，不需要实现为所有三种等待形式定义完全相同的保证或语义，也不需要支持中断线程的实际挂起。
 *
 * <p>An implementation is required to
 * clearly document the semantics and guarantees provided by each of the
 * waiting methods, and when an implementation does support interruption of
 * thread suspension then it must obey the interruption semantics as defined
 * in this interface.
 * 
 * <p> 需要一个实现来清楚地记录每个等待方法提供的语义和保证，并且当实现确实支持中断线程挂起时，
 * 则它必须服从此接口中定义的中断语义。
 *
 * <p>As interruption generally implies cancellation, and checks for
 * interruption are often infrequent, an implementation can favor responding
 * to an interrupt over normal method return. This is true even if it can be
 * shown that the interrupt occurred after another action that may have
 * unblocked the thread. An implementation should document this behavior.
 * 
 * <p> 由于中断通常意味着取消，并且通常不经常进行中断检查，因此与正常方法返回相比，实现可能更喜欢响应中断。
 * 即使可以证明中断发生在另一个可能解除线程阻塞的操作之后，也是如此。实现应记录此行为。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Condition {

    /**
     * Causes the current thread to wait until it is signalled or
     * {@linkplain Thread#interrupt interrupted}.
     * 
     * <p> 使当前线程等待，直到发出信号或被中断为止。
     *
     * <p>The lock associated with this {@code Condition} is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of four things happens:
     * 
     * <p> 与此条件相关联的锁被原子释放，并且出于线程调度目的，当前线程被禁用，并且处于休眠状态，直到发生以下四种情况之一：
     * 
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * 
     * <p> 一些其他线程为此条件调用信号方法，而当前线程恰好被选择为要唤醒的线程；要么
     * 
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * 
     * <p> 某些其他线程为此条件调用signalAll方法；要么
     * 
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * 
     * <p> 某些其他线程中断当前线程，并支持中断线程挂起；要么
     * 
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * 
     * <p> 发生“虚假唤醒”。
     * 
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     * 
     * <p> 在所有情况下，在此方法可以返回之前，当前线程必须重新获取与此条件关联的锁。当线程返回时，可以保证保持此锁。
     *
     * <p>If the current thread:
     * 
     * <p> 如果当前线程：
     * 
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * 
     * <p> 在进入此方法时已设置其中断状态；要么
     * 
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * 
     * <p> 在等待期间中断并支持中断线程，
     * 
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     * 
     * <p> 然后抛出InterruptedException并清除当前线程的中断状态。在第一种情况下，没有规定在释放锁之前是否进行了中断测试。
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     * 
     * <p> 当调用此方法时，假定当前线程持有与此条件关联的锁。由实现来确定是否是这种情况，如果不是，
     * 则如何确定。通常，将引发异常（例如IllegalMonitorStateException），并且实现必须记录该事实。
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal. In that case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     * 
     * <p> 与响应信号的正常方法返回相比，实现可能更喜欢对中断做出响应。在那种情况下，
     * 实现必须确保将信号重定向到另一个等待线程（如果有）。
     *
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         
     * <p> 如果当前线程被中断（并且支持线程挂起的中断）
     */
    void await() throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled.
     * 
     * <p> 使当前线程等待，直到发出信号。
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of three things happens:
     * 
     * <p> 与此条件相关联的锁被原子释放，并且出于线程调度目的，当前线程被禁用，并且处于休眠状态，直到发生以下三种情况之一：
     * 
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * 
     * <p> 一些其他线程为此条件调用信号方法，而当前线程恰好被选择为要唤醒的线程；要么
     * 
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * 
     * <p> 某些其他线程为此条件调用signalAll方法；要么
     * 
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * 
     * <p> 发生“虚假唤醒”。
     * 
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     * 
     * <p> 在所有情况下，在此方法可以返回之前，当前线程必须重新获取与此条件关联的锁。当线程返回时，可以保证保持此锁。
     *
     * <p>If the current thread's interrupted status is set when it enters
     * this method, or it is {@linkplain Thread#interrupt interrupted}
     * while waiting, it will continue to wait until signalled. When it finally
     * returns from this method its interrupted status will still
     * be set.
     * 
     * <p> 如果当前线程进入此方法时设置了中断状态，或者在等待时被中断，它将继续等待直到发出信号。
     * 当它最终从该方法返回时，其中断状态仍将被设置。
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     * 
     * <p> 当调用此方法时，假定当前线程持有与此条件关联的锁。由实现来确定是否是这种情况，如果不是，则如何确定。
     * 通常，将引发异常（例如IllegalMonitorStateException），并且实现必须记录该事实。
     */
    void awaitUninterruptibly();

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified waiting time elapses.
     * 
     * <p> 使当前线程等待，直到发出信号或中断它，或者经过指定的等待时间。
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of five things happens:
     * 
     * <p> 与此条件相关联的锁被原子释放，并且出于线程调度的目的，当前线程被禁用，并且处于休眠状态，直到发生以下五种情况之一：
     * 
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * 
     * <p> 一些其他线程为此条件调用信号方法，而当前线程恰好被选择为要唤醒的线程；要么
     * 
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * 
     * <p> 某些其他线程为此条件调用signalAll方法；要么
     * 
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * 
     * <p> 某些其他线程中断当前线程，并支持中断线程挂起；要么
     * 
     * <li>The specified waiting time elapses; or
     * 
     * <p> 经过指定的等待时间；要么
     * 
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * 
     * <p> 发生“虚假唤醒”。
     * 
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     * 
     * <p> 在所有情况下，在此方法可以返回之前，当前线程必须重新获取与此条件关联的锁。当线程返回时，可以保证保持此锁。
     *
     * <p>If the current thread:
     * 
     * <p> 如果当前线程：
     * 
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * 
     * <p> 在进入此方法时已设置其中断状态；要么
     * 
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * 
     * <p> 在等待期间中断并支持中断线程，
     * 
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     * 
     * <p> 然后抛出InterruptedException并清除当前线程的中断状态。在第一种情况下，
     * 没有规定在释放锁之前是否进行了中断测试。
     *
     * <p>The method returns an estimate of the number of nanoseconds
     * remaining to wait given the supplied {@code nanosTimeout}
     * value upon return, or a value less than or equal to zero if it
     * timed out. This value can be used to determine whether and how
     * long to re-wait in cases where the wait returns but an awaited
     * condition still does not hold. Typical uses of this method take
     * the following form:
     * 
     * <p> 给定返回时提供的nanosTimeout值，该方法将返回等待的剩余纳秒数的估计值；如果超时，
     * 则返回小于或等于零的值。该值可用于确定在等待返回但仍不满足等待条件的情况下是否重新等待以及等待多
     * 长时间。此方法的典型用法采用以下形式：
     *
     *  <pre> {@code
     * boolean aMethod(long timeout, TimeUnit unit) {
     *   long nanos = unit.toNanos(timeout);
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (nanos <= 0L)
     *         return false;
     *       nanos = theCondition.awaitNanos(nanos);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * <p>Design note: This method requires a nanosecond argument so
     * as to avoid truncation errors in reporting remaining times.
     * Such precision loss would make it difficult for programmers to
     * ensure that total waiting times are not systematically shorter
     * than specified when re-waits occur.
     * 
     * <p> 设计说明：此方法需要一个纳秒参数，以避免在报告剩余时间时出现截断错误。
     *  这样的精度损失将使程序员难以确保总的等待时间不会系统地短于重新等待发生时指定的时间。
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     * 
     * <p> 当调用此方法时，假定当前线程持有与此条件关联的锁。 由实现来确定是否是这种情况，如果不是，
     * 则如何确定。 通常，将引发异常（例如IllegalMonitorStateException），并且实现必须记录该事实。
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal, or over indicating the elapse
     * of the specified waiting time. In either case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     * 
     * <p> 与正常方法返回响应信号相比，或者与指示经过指定的等待时间相比，实现可能更喜欢对中断做出响应。 
     * 无论哪种情况，实现都必须确保将信号重定向到另一个等待线程（如果有）。
     *
     * @param nanosTimeout the maximum time to wait, in nanoseconds
     * 
     * <p> 等待的最长时间（以纳秒为单位）
     * 
     * @return an estimate of the {@code nanosTimeout} value minus
     *         the time spent waiting upon return from this method.
     *         A positive value may be used as the argument to a
     *         subsequent call to this method to finish waiting out
     *         the desired time.  A value less than or equal to zero
     *         indicates that no time remains.
     *         
     * <p> nanosTimeout值的估计值减去从此方法返回之前等待的时间。 可以将正值用作对该方法的后续调用的参数，
     * 以完成等待所需的时间。 小于或等于零的值表示没有时间剩余。
     * 
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         
     * <p> 如果当前线程被中断（并且支持线程挂起的中断）
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified waiting time elapses. This method is behaviorally
     * equivalent to:
     * 
     * <p> 使当前线程等待，直到发出信号或中断它，或者经过指定的等待时间。 此方法在行为上等效于：
     * 
     *  <pre> {@code awaitNanos(unit.toNanos(time)) > 0}</pre>
     *
     * @param time the maximum time to wait
     * 
     * <p> 等待的最长时间
     * 
     * @param unit the time unit of the {@code time} argument
     * 
     * <p> 时间参数的时间单位
     * 
     * @return {@code false} if the waiting time detectably elapsed
     *         before return from the method, else {@code true}
     *         
     * <p> 如果从方法返回之前等待时间已过，则返回false；否则返回true
     * 
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         
     * <p> 如果当前线程被中断（并且支持线程挂起的中断）
     */
    boolean await(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Causes the current thread to wait until it is signalled or interrupted,
     * or the specified deadline elapses.
     * 
     * <p> 使当前线程等待，直到发出信号或被中断或指定的截止时间过去为止。
     *
     * <p>The lock associated with this condition is atomically
     * released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of five things happens:
     * 
     * <p> 与此条件相关联的锁被原子释放，并且出于线程调度的目的，当前线程被禁用，并且处于休眠状态，
     * 直到发生以下五种情况之一：
     * 
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this
     * {@code Condition} and the current thread happens to be chosen as the
     * thread to be awakened; or
     * 
     * <p> 一些其他线程为此条件调用信号方法，而当前线程恰好被选择为要唤醒的线程；要么
     * 
     * <li>Some other thread invokes the {@link #signalAll} method for this
     * {@code Condition}; or
     * 
     * <p> 某些其他线程为此条件调用signalAll方法；要么
     * 
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread, and interruption of thread suspension is supported; or
     * 
     * <p> 某些其他线程中断当前线程，并支持中断线程挂起；要么
     * 
     * <li>The specified deadline elapses; or
     * 
     * <p> 指定的期限已过；要么
     * 
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * 
     * <p> 发生“虚假唤醒”。
     * 
     * </ul>
     *
     * <p>In all cases, before this method can return the current thread must
     * re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     *
     * <p> 在所有情况下，在此方法可以返回之前，当前线程必须重新获取与此条件关联的锁。当线程返回时，
     * 可以保证保持此锁。
     *
     * <p>If the current thread:
     * 
     * <p> 如果当前线程：
     * 
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * 
     * <p> 在进入此方法时已设置其中断状态；要么
     * 
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * and interruption of thread suspension is supported,
     * 
     * <p> 在等待期间中断并支持中断线程，
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock
     * is released.
     *
     * <p> 然后抛出InterruptedException并清除当前线程的中断状态。在第一种情况下，
     * 没有规定在释放锁之前是否进行了中断测试。
     *
     * <p>The return value indicates whether the deadline has elapsed,
     * which can be used as follows:
     * 
     * <p> 返回值指示期限是否已过，可以按以下方式使用：
     * 
     *  <pre> {@code
     * boolean aMethod(Date deadline) {
     *   boolean stillWaiting = true;
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (!stillWaiting)
     *         return false;
     *       stillWaiting = theCondition.awaitUntil(deadline);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>The current thread is assumed to hold the lock associated with this
     * {@code Condition} when this method is called.
     * It is up to the implementation to determine if this is
     * the case and if not, how to respond. Typically, an exception will be
     * thrown (such as {@link IllegalMonitorStateException}) and the
     * implementation must document that fact.
     * 
     * <p> 当调用此方法时，假定当前线程持有与此条件关联的锁。 由实现来确定是否是这种情况，如果不是，则如何确定。 
     * 通常，将引发异常（例如IllegalMonitorStateException），并且实现必须记录该事实。
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return in response to a signal, or over indicating the passing
     * of the specified deadline. In either case the implementation
     * must ensure that the signal is redirected to another waiting thread, if
     * there is one.
     * 
     * <p> 与正常方法返回响应信号相比，或者与指示经过指定的截止日期相比，实现可能更喜欢对中断做出响应。 
     * 无论哪种情况，实现都必须确保将信号重定向到另一个等待线程（如果有）。
     *
     * @param deadline the absolute time to wait until
     * 
     * <p> 等到的绝对时间
     * 
     * @return {@code false} if the deadline has elapsed upon return, else
     *         {@code true}
     *         
     * <p> 如果最后期限已过，则返回false
     * 
     * @throws InterruptedException if the current thread is interrupted
     *         (and interruption of thread suspension is supported)
     *         
     * <p> 如果当前线程被中断（并且支持线程挂起的中断）
     */
    boolean awaitUntil(Date deadline) throws InterruptedException;

    /**
     * Wakes up one waiting thread.
     * 
     * <p> 唤醒一个等待线程。
     *
     * <p>If any threads are waiting on this condition then one
     * is selected for waking up. That thread must then re-acquire the
     * lock before returning from {@code await}.
     * 
     * <p> 如果有任何线程在这种情况下等待，则选择一个线程进行唤醒。 然后，该线程必须重新获取锁，
     * 然后才能从等待返回。
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>An implementation may (and typically does) require that the
     * current thread hold the lock associated with this {@code
     * Condition} when this method is called. Implementations must
     * document this precondition and any actions taken if the lock is
     * not held. Typically, an exception such as {@link
     * IllegalMonitorStateException} will be thrown.
     * 
     * <p> 一个实现可能（并且通常确实）要求在调用此方法时当前线程持有与此Condition相关的锁。
     *  实现必须记录此先决条件，以及如果未持有锁，则应采取的任何措施。 通常，将引发诸如
     *  IllegalMonitorStateException之类的异常。
     */
    void signal();

    /**
     * Wakes up all waiting threads.
     * 
     * <p> 唤醒所有等待的线程。
     *
     * <p>If any threads are waiting on this condition then they are
     * all woken up. Each thread must re-acquire the lock before it can
     * return from {@code await}.
     * 
     * <p> 如果有任何线程在这种情况下等待，那么它们都将被唤醒。 每个线程必须重新获取锁，
     * 然后才能从等待状态返回。
     *
     * <p><b>Implementation Considerations</b>
     * 
     * <p> 实施注意事项
     *
     * <p>An implementation may (and typically does) require that the
     * current thread hold the lock associated with this {@code
     * Condition} when this method is called. Implementations must
     * document this precondition and any actions taken if the lock is
     * not held. Typically, an exception such as {@link
     * IllegalMonitorStateException} will be thrown.
     * 
     * <p> 一个实现可能（并且通常确实）要求在调用此方法时当前线程持有与此Condition相关的锁。 
     * 实现必须记录此先决条件，以及如果未持有锁，则应采取的任何措施。 通常，将引发诸如
     * IllegalMonitorStateException之类的异常。
     */
    void signalAll();
}
