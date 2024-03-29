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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * Provides a framework for implementing blocking locks and related
 * synchronizers (semaphores, events, etc) that rely on
 * first-in-first-out (FIFO) wait queues.  This class is designed to
 * be a useful basis for most kinds of synchronizers that rely on a
 * single atomic {@code int} value to represent state. Subclasses
 * must define the protected methods that change this state, and which
 * define what that state means in terms of this object being acquired
 * or released.  Given these, the other methods in this class carry
 * out all queuing and blocking mechanics. Subclasses can maintain
 * other state fields, but only the atomically updated {@code int}
 * value manipulated using methods {@link #getState}, {@link
 * #setState} and {@link #compareAndSetState} is tracked with respect
 * to synchronization.
 * 
 * <p> 提供一个框架，用于实现依赖于先进先出（FIFO）等待队列的阻塞锁和相关的同步器（信号灯，事件等）。
 * 此类旨在为大多数依赖单个原子int值表示状态的同步器提供有用的基础。子类必须定义更改此状态的受保护方法，
 * 并定义该状态对于获取或释放此对象而言意味着什么。鉴于这些，此类中的其他方法将执行所有排队和阻塞机制。
 * 子类可以维护其他状态字段，但是就同步而言，仅跟踪使用方法getState，setState和compareAndSetState
 * 操作的原子更新的int值。
 *
 * <p>Subclasses should be defined as non-public internal helper
 * classes that are used to implement the synchronization properties
 * of their enclosing class.  Class
 * {@code AbstractQueuedSynchronizer} does not implement any
 * synchronization interface.  Instead it defines methods such as
 * {@link #acquireInterruptibly} that can be invoked as
 * appropriate by concrete locks and related synchronizers to
 * implement their public methods.
 * 
 * <p> 子类应定义为用于实现其所在类的同步属性的非公共内部帮助器类。类AbstractQueuedSynchronizer不实现任何同步接口。
 * 相反，它定义了诸如acquireInterruptible之类的方法，可以通过具体的锁和相关的同步器适当地调用这些方法来实现其公共方法。
 *
 * <p>This class supports either or both a default <em>exclusive</em>
 * mode and a <em>shared</em> mode. When acquired in exclusive mode,
 * attempted acquires by other threads cannot succeed. Shared mode
 * acquires by multiple threads may (but need not) succeed. This class
 * does not &quot;understand&quot; these differences except in the
 * mechanical sense that when a shared mode acquire succeeds, the next
 * waiting thread (if one exists) must also determine whether it can
 * acquire as well. Threads waiting in the different modes share the
 * same FIFO queue. Usually, implementation subclasses support only
 * one of these modes, but both can come into play for example in a
 * {@link ReadWriteLock}. Subclasses that support only exclusive or
 * only shared modes need not define the methods supporting the unused mode.
 *
 * <p> 此类支持默认排他模式和共享模式之一或两者。当以独占方式进行获取时，其他线程尝试进行的获取将无法成功。
 * 由多个线程获取的共享模式可能（但不一定）成功。该类不“理解”这些差异，只是从机械意义上说，当共享模式获取成功时，
 * 下一个等待线程（如果存在）还必须确定它是否也可以获取。在不同模式下等待的线程共享相同的FIFO队列。
 * 通常，实现子类仅支持这些模式之一，但例如可以在ReadWriteLock中发挥作用。
 * 仅支持互斥模式或仅共享模式的子类无需定义支持未使用模式的方法。
 * 
 * <p>This class defines a nested {@link ConditionObject} class that
 * can be used as a {@link Condition} implementation by subclasses
 * supporting exclusive mode for which method {@link
 * #isHeldExclusively} reports whether synchronization is exclusively
 * held with respect to the current thread, method {@link #release}
 * invoked with the current {@link #getState} value fully releases
 * this object, and {@link #acquire}, given this saved state value,
 * eventually restores this object to its previous acquired state.  No
 * {@code AbstractQueuedSynchronizer} method otherwise creates such a
 * condition, so if this constraint cannot be met, do not use it.  The
 * behavior of {@link ConditionObject} depends of course on the
 * semantics of its synchronizer implementation.
 * 
 * <p> 此类定义了一个嵌套的ConditionObject类，可由支持独占模式的子类用作条件实现，
 * 该子类支持独占模式，方法isHeldExclusively报告是否相对于当前线程独占保持同步，
 * 使用当前getState值调用的方法release完全释放该对象，并在给定此保存状态值的情况下进行获取，
 * 最终将该对象恢复为其先前获取的状态。否则，没有AbstractQueuedSynchronizer方法会创建这样的条件，
 * 因此，如果无法满足此约束，请不要使用它。 ConditionObject的行为当然取决于其同步器实现的语义。
 *
 * <p>This class provides inspection, instrumentation, and monitoring
 * methods for the internal queue, as well as similar methods for
 * condition objects. These can be exported as desired into classes
 * using an {@code AbstractQueuedSynchronizer} for their
 * synchronization mechanics.
 * 
 * <p> 此类提供了内部队列的检查，检测和监视方法，以及条件对象的类似方法。可以根据需要使用
 * AbstractQueuedSynchronizer将它们导出到类中以实现其同步机制。
 *
 * <p>Serialization of this class stores only the underlying atomic
 * integer maintaining state, so deserialized objects have empty
 * thread queues. Typical subclasses requiring serializability will
 * define a {@code readObject} method that restores this to a known
 * initial state upon deserialization.
 * 
 * <p> 此类的序列化仅存储基础原子整数维护状态，因此反序列化的对象具有空线程队列。需要序列化性的典型子
 * 类将定义一个readObject方法，该方法在反序列化时将其恢复为已知的初始状态。
 *
 * <h3>Usage</h3>
 * 
 * <p> 用法
 *
 * <p>To use this class as the basis of a synchronizer, redefine the
 * following methods, as applicable, by inspecting and/or modifying
 * the synchronization state using {@link #getState}, {@link
 * #setState} and/or {@link #compareAndSetState}:
 * 
 * <p> 要将此类用作同步器的基础，请通过使用getState，setState和/或compareAndSetState
 * 检查和/或修改同步状态来重新定义以下方法（如适用）：
 *
 * <ul>
 * <li> {@link #tryAcquire}
 * <li> {@link #tryRelease}
 * <li> {@link #tryAcquireShared}
 * <li> {@link #tryReleaseShared}
 * <li> {@link #isHeldExclusively}
 * </ul>
 *
 * Each of these methods by default throws {@link
 * UnsupportedOperationException}.  Implementations of these methods
 * must be internally thread-safe, and should in general be short and
 * not block. Defining these methods is the <em>only</em> supported
 * means of using this class. All other methods are declared
 * {@code final} because they cannot be independently varied.
 * 
 * <p> 默认情况下，这些方法中的每一个都会引发UnsupportedOperationException。 这些方法的实现必须在内部是线程安全的，
 * 并且通常应简短且不阻塞。 定义这些方法是使用此类的唯一受支持的方法。 所有其他方法都被声明为最终方法，因为它们不能独立变化。
 *
 * <p>You may also find the inherited methods from {@link
 * AbstractOwnableSynchronizer} useful to keep track of the thread
 * owning an exclusive synchronizer.  You are encouraged to use them
 * -- this enables monitoring and diagnostic tools to assist users in
 * determining which threads hold locks.
 * 
 * <p> 您可能还会发现从AbstractOwnableSynchronizer继承的方法对跟踪拥有独占同步器的线程很有用。 
 * 鼓励您使用它们-这将启用监视和诊断工具，以帮助用户确定哪些线程持有锁。
 *
 * <p>Even though this class is based on an internal FIFO queue, it
 * does not automatically enforce FIFO acquisition policies.  The core
 * of exclusive synchronization takes the form:
 * 
 * <p> 即使此类基于内部FIFO队列，它也不会自动执行FIFO获取策略。 独占同步的核心采用以下形式：
 *
 * <pre>
 * Acquire:
 *     while (!tryAcquire(arg)) {
 *        <em>enqueue thread if it is not already queued</em>;
 *        <em>possibly block current thread</em>;
 *     }
 *
 * Release:
 *     if (tryRelease(arg))
 *        <em>unblock the first queued thread</em>;
 * </pre>
 *
 * (Shared mode is similar but may involve cascading signals.)
 * 
 * <p> （共享模式相似，但可能涉及级联信号。）
 *
 * <p id="barging">Because checks in acquire are invoked before
 * enqueuing, a newly acquiring thread may <em>barge</em> ahead of
 * others that are blocked and queued.  However, you can, if desired,
 * define {@code tryAcquire} and/or {@code tryAcquireShared} to
 * disable barging by internally invoking one or more of the inspection
 * methods, thereby providing a <em>fair</em> FIFO acquisition order.
 * In particular, most fair synchronizers can define {@code tryAcquire}
 * to return {@code false} if {@link #hasQueuedPredecessors} (a method
 * specifically designed to be used by fair synchronizers) returns
 * {@code true}.  Other variations are possible.
 * 
 * <p> 因为获取队列中的入库检查是在排队之前被调用的，所以新获取线程可能会在被阻塞和排队的其他线程之前插入。
 * 但是，如果需要，您可以定义tryAcquire和/或tryAcquireShared以通过内部调用一种或多种检查方法来
 * 禁用插入，从而提供公平的FIFO获取顺序。特别是，如果hasQueuedPredecessors（一种专门设计用于公平
 * 同步器的方法）返回true，则大多数公平同步器都可以定义tryAcquire返回false。其他变化是可能的。
 *
 * <p>Throughput and scalability are generally highest for the
 * default barging (also known as <em>greedy</em>,
 * <em>renouncement</em>, and <em>convoy-avoidance</em>) strategy.
 * While this is not guaranteed to be fair or starvation-free, earlier
 * queued threads are allowed to recontend before later queued
 * threads, and each recontention has an unbiased chance to succeed
 * against incoming threads.  Also, while acquires do not
 * &quot;spin&quot; in the usual sense, they may perform multiple
 * invocations of {@code tryAcquire} interspersed with other
 * computations before blocking.  This gives most of the benefits of
 * spins when exclusive synchronization is only briefly held, without
 * most of the liabilities when it isn't. If so desired, you can
 * augment this by preceding calls to acquire methods with
 * "fast-path" checks, possibly prechecking {@link #hasContended}
 * and/or {@link #hasQueuedThreads} to only do so if the synchronizer
 * is likely not to be contended.
 * 
 * <p> 对于默认插入（也称为贪婪，放弃和避免车队）策略，吞吐量和可伸缩性通常最高。尽管不能保证这是公平的，
 * 也可以避免饥饿，但允许在较早排队的线程之前重新竞争较早排队的线程，并且每个重新争用都可以毫无偏向地成功
 * 抵御传入线程。同样，尽管获取通常不是“旋转”的，但是在阻塞之前，它们可能会执行tryAcquire的多次调用，
 * 并插入其他计算。如果仅短暂地保持排他同步，则这将带来旋转的大部分好处，而如果不进行排他同步，则不会带来很
 * 多负担。如果需要的话，可以通过在调用之前使用“快速路径”检查来获取方法来增强此功能，并可能预先检查
 * hasContended和/或hasQueuedThreads以仅在可能不争用同步器的情况下这样做。
 *
 * <p>This class provides an efficient and scalable basis for
 * synchronization in part by specializing its range of use to
 * synchronizers that can rely on {@code int} state, acquire, and
 * release parameters, and an internal FIFO wait queue. When this does
 * not suffice, you can build synchronizers from a lower level using
 * {@link java.util.concurrent.atomic atomic} classes, your own custom
 * {@link java.util.Queue} classes, and {@link LockSupport} blocking
 * support.
 * 
 * <p> 此类为同步提供了有效且可扩展的基础，部分原因是通过将其使用范围专用于可以依赖于int状态，
 * 获取和释放参数以及内部FIFO等待队列的同步器。如果这还不足够，则可以使用原子类，您自己的自定义
 * java.util.Queue类和LockSupport阻止支持从较低级别构建同步器。
 *
 * <h3>Usage Examples</h3>
 * 
 * <p> 使用范例
 *
 * <p>Here is a non-reentrant mutual exclusion lock class that uses
 * the value zero to represent the unlocked state, and one to
 * represent the locked state. While a non-reentrant lock
 * does not strictly require recording of the current owner
 * thread, this class does so anyway to make usage easier to monitor.
 * It also supports conditions and exposes
 * one of the instrumentation methods:
 * 
 * <p> 这是一个不可重入的互斥锁定类，使用值0表示解锁状态，使用值1表示锁定状态。尽管不可重入锁并不
 * 严格要求记录当前所有者线程，但无论如何，此类会这样做，以使使用情况更易于监视。
 * 它还支持条件并公开一种检测方法：
 *
 *  <pre> {@code
 * class Mutex implements Lock, java.io.Serializable {
 *
 *   // Our internal helper class
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     // Reports whether in locked state
 *     protected boolean isHeldExclusively() {
 *       return getState() == 1;
 *     }
 *
 *     // Acquires the lock if state is zero
 *     public boolean tryAcquire(int acquires) {
 *       assert acquires == 1; // Otherwise unused
 *       if (compareAndSetState(0, 1)) {
 *         setExclusiveOwnerThread(Thread.currentThread());
 *         return true;
 *       }
 *       return false;
 *     }
 *
 *     // Releases the lock by setting state to zero
 *     protected boolean tryRelease(int releases) {
 *       assert releases == 1; // Otherwise unused
 *       if (getState() == 0) throw new IllegalMonitorStateException();
 *       setExclusiveOwnerThread(null);
 *       setState(0);
 *       return true;
 *     }
 *
 *     // Provides a Condition
 *     Condition newCondition() { return new ConditionObject(); }
 *
 *     // Deserializes properly
 *     private void readObject(ObjectInputStream s)
 *         throws IOException, ClassNotFoundException {
 *       s.defaultReadObject();
 *       setState(0); // reset to unlocked state
 *     }
 *   }
 *
 *   // The sync object does all the hard work. We just forward to it.
 *   private final Sync sync = new Sync();
 *
 *   public void lock()                { sync.acquire(1); }
 *   public boolean tryLock()          { return sync.tryAcquire(1); }
 *   public void unlock()              { sync.release(1); }
 *   public Condition newCondition()   { return sync.newCondition(); }
 *   public boolean isLocked()         { return sync.isHeldExclusively(); }
 *   public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
 *   public void lockInterruptibly() throws InterruptedException {
 *     sync.acquireInterruptibly(1);
 *   }
 *   public boolean tryLock(long timeout, TimeUnit unit)
 *       throws InterruptedException {
 *     return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 *   }
 * }}</pre>
 *
 * <p>Here is a latch class that is like a
 * {@link java.util.concurrent.CountDownLatch CountDownLatch}
 * except that it only requires a single {@code signal} to
 * fire. Because a latch is non-exclusive, it uses the {@code shared}
 * acquire and release methods.
 * 
 * <p> 这是一个类似于CountDownLatch的闩锁类，只不过它只需要触发一个信号即可。 因为闩锁是非排他性的，所以它使用共享的获取和释放方法。
 *
 *  <pre> {@code
 * class BooleanLatch {
 *
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     boolean isSignalled() { return getState() != 0; }
 *
 *     protected int tryAcquireShared(int ignore) {
 *       return isSignalled() ? 1 : -1;
 *     }
 *
 *     protected boolean tryReleaseShared(int ignore) {
 *       setState(1);
 *       return true;
 *     }
 *   }
 *
 *   private final Sync sync = new Sync();
 *   public boolean isSignalled() { return sync.isSignalled(); }
 *   public void signal()         { sync.releaseShared(1); }
 *   public void await() throws InterruptedException {
 *     sync.acquireSharedInterruptibly(1);
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    /**
     * Creates a new {@code AbstractQueuedSynchronizer} instance
     * with initial synchronization state of zero.
     * 
     * <p> 创建一个新的AbstractQueuedSynchronizer实例，其初始同步状态为零。
     */
    protected AbstractQueuedSynchronizer() { }

    /**
     * Wait queue node class.
     * 
     * <p> 等待队列节点类。
     *
     * <p>The wait queue is a variant of a "CLH" (Craig, Landin, and
     * Hagersten) lock queue. CLH locks are normally used for
     * spinlocks.  We instead use them for blocking synchronizers, but
     * use the same basic tactic of holding some of the control
     * information about a thread in the predecessor of its node.  A
     * "status" field in each node keeps track of whether a thread
     * should block.  A node is signalled when its predecessor
     * releases.  Each node of the queue otherwise serves as a
     * specific-notification-style monitor holding a single waiting
     * thread. The status field does NOT control whether threads are
     * granted locks etc though.  A thread may try to acquire if it is
     * first in the queue. But being first does not guarantee success;
     * it only gives the right to contend.  So the currently released
     * contender thread may need to rewait.
     * 
     * <p> 等待队列是“ CLH”（Craig，Landin和Hagersten）锁定队列的变体。 CLH锁通常用于自旋锁。 
     * 相反，我们将它们用于阻塞同步器，但是使用相同的基本策略，即在其节点的前身中保存有关线程的某些控制信息。 
     * 每个节点中的“状态”字段将跟踪线程是否应阻塞。 节点的前任释放时会发出信号。 否则，队列的每个节点都将用作
     * 持有单个等待线程的特定通知样式的监视器。 虽然状态字段不控制是否授予线程锁等。 线程可能会尝试获取它是否在
     * 队列中的第一位。 但是先行并不能保证成功。 它只赋予了抗辩的权利。 因此，当前发布的竞争者线程可能需要重新等待。
     *
     * <p>To enqueue into a CLH lock, you atomically splice it in as new
     * tail. To dequeue, you just set the head field.
     * 
     * <p> 要加入CLH锁，您可以自动将其作为新尾部进行拼接。 要出队，您只需设置头字段。
     * 
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
     *
     * <p>Insertion into a CLH queue requires only a single atomic
     * operation on "tail", so there is a simple atomic point of
     * demarcation from unqueued to queued. Similarly, dequeuing
     * involves only updating the "head". However, it takes a bit
     * more work for nodes to determine who their successors are,
     * in part to deal with possible cancellation due to timeouts
     * and interrupts.
     * 
     * <p> 插入到CLH队列中，只需要对“尾巴”执行一次原子操作，因此存在一个简单的原子分界点，
     * 即从未排队到排队。同样，出队仅涉及更新“头”。但是，要确定节点的后继者是谁，需要花费更多的精力，
     * 部分原因是要处理由于超时和中断而可能导致的取消。
     *
     * <p>The "prev" links (not used in original CLH locks), are mainly
     * needed to handle cancellation. If a node is cancelled, its
     * successor is (normally) relinked to a non-cancelled
     * predecessor. For explanation of similar mechanics in the case
     * of spin locks, see the papers by Scott and Scherer at
     * http://www.cs.rochester.edu/u/scott/synchronization/
     * 
     * <p> “ prev”链接（在原始CLH锁中不使用）主要用于处理取消。如果取消某个节点，则其后继节点（通常）会重新链接到未取消的前任节点。
     * 有关自旋锁情况下类似机制的说明，请参见Scott和Scherer的论文，
     * 网址为http://www.cs.rochester.edu/u/scott/synchronization/
     *
     * <p>We also use "next" links to implement blocking mechanics.
     * The thread id for each node is kept in its own node, so a
     * predecessor signals the next node to wake up by traversing
     * next link to determine which thread it is.  Determination of
     * successor must avoid races with newly queued nodes to set
     * the "next" fields of their predecessors.  This is solved
     * when necessary by checking backwards from the atomically
     * updated "tail" when a node's successor appears to be null.
     * (Or, said differently, the next-links are an optimization
     * so that we don't usually need a backward scan.)
     * 
     * <p> 我们还使用“下一个”链接来实现阻止机制。每个节点的线程ID都保留在其自己的节点中，
     * 因此前任通过遍历下一个链接以确定它是哪个线程，从而通知下一个节点唤醒。确定后继者必须避免与新
     * 排队的节点竞争来设置其前任节点的“ next”字段。在必要时，可以通过在节点的后继者似乎为空时从
     * 原子更新的“尾部”向后检查来解决此问题。 （或者换句话说，下一个链接是一种优化，
     * 因此我们通常不需要向后扫描。）
     *
     * <p>Cancellation introduces some conservatism to the basic
     * algorithms.  Since we must poll for cancellation of other
     * nodes, we can miss noticing whether a cancelled node is
     * ahead or behind us. This is dealt with by always unparking
     * successors upon cancellation, allowing them to stabilize on
     * a new predecessor, unless we can identify an uncancelled
     * predecessor who will carry this responsibility.
     * 
     * <p> 取消将一些保守性引入了基本算法。由于我们必须轮询其他节点的取消，因此我们可能会遗漏一个
     * 被取消的节点在我们前面还是后面。要解决此问题，必须始终在取消合同时取消继任者，使他们能够稳定
     * 在新的前任身上，除非我们能够确定一个将要承担此责任的前任取消。
     *
     * <p>CLH queues need a dummy header node to get started. But
     * we don't create them on construction, because it would be wasted
     * effort if there is never contention. Instead, the node
     * is constructed and head and tail pointers are set upon first
     * contention.
     * 
     * <p> CLH队列需要一个虚拟头节点节点才能开始。但是，我们不会在构建过程中创建它们，因为如果没有争用，
     * 这将是徒劳的。而是构造节点，并在第一次争用时设置头和尾指针。
     *
     * <p>Threads waiting on Conditions use the same nodes, but
     * use an additional link. Conditions only need to link nodes
     * in simple (non-concurrent) linked queues because they are
     * only accessed when exclusively held.  Upon await, a node is
     * inserted into a condition queue.  Upon signal, the node is
     * transferred to the main queue.  A special value of status
     * field is used to mark which queue a node is on.
     * 
     * <p> 等待条件的线程使用相同的节点，但是使用附加的链接。条件只需要在简单（非并行）链接队列中链接节点，
     * 因为只有在专用时才可以访问它们。等待时，将节点插入条件队列。收到信号后，该节点将转移到主队列。
     * 状态字段的特殊值用于标记节点所在的队列。
     *
     * <p>Thanks go to Dave Dice, Mark Moir, Victor Luchangco, Bill
     * Scherer and Michael Scott, along with members of JSR-166
     * expert group, for helpful ideas, discussions, and critiques
     * on the design of this class.
     * 
     * <p> 感谢Dave Dice，Mark Moir，Victor Luchangco，Bill Scherer和Michael Scott
     * 以及JSR-166专家组的成员，对此类的设计提出了有益的想法，讨论和批评。
     */
    static final class Node {
        /** 
         * Marker to indicate a node is waiting in shared mode 
         * 
         * <p> 指示节点正在共享模式下等待的标记
         * 
         */
        static final Node SHARED = new Node();
        /** 
         * 
         * Marker to indicate a node is waiting in exclusive mode 
         * 
         * <p> 指示节点正在以独占模式等待的标记
         */
        static final Node EXCLUSIVE = null;

        /** 
         * waitStatus value to indicate thread has cancelled 
         * 
         * <p> waitStatus值，指示线程已取消
         */
        static final int CANCELLED =  1;
        /** 
         * 
         * waitStatus value to indicate successor's thread needs unparking 
         * 
         * <p> waitStatus值，指示后续线程需要释放
         */
        static final int SIGNAL    = -1;
        /** 
         * waitStatus value to indicate thread is waiting on condition 
         * 
         * <p> waitStatus值，指示线程正在等待条件
         */
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         * 
         * <p> waitStatus值，指示下一个acquireShared应该无条件传播
         */
        static final int PROPAGATE = -3;

        /**
         * Status field, taking on only the values:
         * 
         * <p> 状态字段，仅采用以下值：
         * 
         *   <p> SIGNAL:     The successor of this node is (or will soon be)
         *               blocked (via park), so the current node must
         *               unpark its successor when it releases or
         *               cancels. To avoid races, acquire methods must
         *               first indicate they need a signal,
         *               then retry the atomic acquire, and then,
         *               on failure, block.
         *               
         *   <p> 信号：此节点的后继者被（或将很快被阻止）（通过停放），因此当前节点释放或取消时必须取消其后继者的停放。
         *   为了避免种族冲突，acquire方法必须首先表明它们需要信号，然后重试原子获取，然后在失败时阻塞。
         *   
         *   <p> CANCELLED:  This node is cancelled due to timeout or interrupt.
         *               Nodes never leave this state. In particular,
         *               a thread with cancelled node never again blocks.
         *               
         *   <p> 取消：由于超时或中断，该节点被取消。节点永远不会离开此状态。特别是，具有取消节点的线程永远不会再次阻塞。
         *   
         *   <p> CONDITION:  This node is currently on a condition queue.
         *               It will not be used as a sync queue node
         *               until transferred, at which time the status
         *               will be set to 0. (Use of this value here has
         *               nothing to do with the other uses of the
         *               field, but simplifies mechanics.)
         *               
         *   <p> 条件：该节点当前在条件队列中。在传输之前，它不会用作同步队列节点，此时状态将设置为0。
         *   （此值的使用与该字段的其他用途无关，但简化了机制。）
         *   
         *   <p> PROPAGATE:  A releaseShared should be propagated to other
         *               nodes. This is set (for head node only) in
         *               doReleaseShared to ensure propagation
         *               continues, even if other operations have
         *               since intervened.
         *               
         *   <p> 传播：releaseShared应该传播到其他节点。在doReleaseShared中对此进行了设置（仅适用于头节点），
         *   以确保传播继续进行，即使此后进行了其他操作也是如此。
         *   
         *   <p> 0:          None of the above
         *   
         *   <p> 0：以上都不是
         *
         * <p> The values are arranged numerically to simplify use.
         * Non-negative values mean that a node doesn't need to
         * signal. So, most code doesn't need to check for particular
         * values, just for sign.
         * 
         * <p> 这些值以数字方式排列以简化使用。非负值表示节点不需要发信号。因此，大多数代码不需要检查特定值，
         * 仅需检查符号即可。
         *
         * <p> The field is initialized to 0 for normal sync nodes, and
         * CONDITION for condition nodes.  It is modified using CAS
         * (or when possible, unconditional volatile writes).
         * 
         * <p> 对于普通同步节点，该字段初始化为0，对于条件节点，该字段初始化为CONDITION。
         * 使用CAS（或在可能的情况下进行无条件的易失性写操作）对其进行修改。
         */
        volatile int waitStatus;

        /**
         * Link to predecessor node that current node/thread relies on
         * for checking waitStatus. Assigned during enqueuing, and nulled
         * out (for sake of GC) only upon dequeuing.  Also, upon
         * cancellation of a predecessor, we short-circuit while
         * finding a non-cancelled one, which will always exist
         * because the head node is never cancelled: A node becomes
         * head only as a result of successful acquire. A
         * cancelled thread never succeeds in acquiring, and a thread only
         * cancels itself, not any other node.
         * 
         * <p> 链接到当前节点/线程用来检查waitStatus的先前节点。 在入队期间分配，
         * 并且仅在出队时将其清空（出于GC的考虑）。 同样，在取消前任后，我们会短路，
         * 同时找到一个未取消的前任，这将始终存在，因为根节点永远不会被取消：只有成功获取后，结点才变为根。
         *  取消的线程永远不会成功获取，并且线程只会取消自身，不会取消任何其他节点。
         */
        volatile Node prev;

        /**
         * Link to the successor node that the current node/thread
         * unparks upon release. Assigned during enqueuing, adjusted
         * when bypassing cancelled predecessors, and nulled out (for
         * sake of GC) when dequeued.  The enq operation does not
         * assign next field of a predecessor until after attachment,
         * so seeing a null next field does not necessarily mean that
         * node is at end of queue. However, if a next field appears
         * to be null, we can scan prev's from the tail to
         * double-check.  The next field of cancelled nodes is set to
         * point to the node itself instead of null, to make life
         * easier for isOnSyncQueue.
         * 
         * <p> 链接到后继节点，当前节点/线程在释放时将其解散。 在排队过程中分配，在绕过已取消的前辈时进行调整，
         * 在出队时清零（出于GC的考虑）。 enq操作直到附加后才分配前任的下一个字段，因此看到空的下一个字段并不一
         * 定意味着该节点位于队列末尾。 但是，如果下一个字段显示为空，则我们可以从尾部扫描上一个以进行再次检查。 
         * 已取消节点的下一个字段设置为指向节点本身而不是null，以使isOnSyncQueue的工作更轻松。
         */
        volatile Node next;

        /**
         * The thread that enqueued this node.  Initialized on
         * construction and nulled out after use.
         * 
         * <p> 使该节点排队的线程。 在构造上初始化，使用后消失。
         */
        volatile Thread thread;

        /**
         * Link to next node waiting on condition, or the special
         * value SHARED.  Because condition queues are accessed only
         * when holding in exclusive mode, we just need a simple
         * linked queue to hold nodes while they are waiting on
         * conditions. They are then transferred to the queue to
         * re-acquire. And because conditions can only be exclusive,
         * we save a field by using special value to indicate shared
         * mode.
         * 
         * <p> 链接到等待条件的下一个节点，或者链接到特殊值SHARED。 由于条件队列仅在以独占模式保存时才被访问，
         * 因此我们只需要一个简单的链接队列即可在节点等待条件时保存节点。 然后将它们转移到队列中以重新获取。 
         * 并且由于条件只能是互斥的，因此我们使用特殊值来表示共享模式来保存字段。
         */
        Node nextWaiter;

        /**
         * Returns true if node is waiting in shared mode.
         * 
         * <p> 如果节点在共享模式下等待，则返回true。
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * Returns previous node, or throws NullPointerException if null.
         * Use when predecessor cannot be null.  The null check could
         * be elided, but is present to help the VM.
         * 
         * <p> 返回上一个节点，如果为null，则抛出NullPointerException。 
         * 当前任不能为null时使用。 空检查可能会被忽略，但是它可以帮助VM。
         *
         * @return the predecessor of this node
         * 
         * <p> 该节点的前身
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // Used to establish initial head or SHARED marker - 用于建立初始标头或SHARED标记
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter - 由addWaiter使用
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition - 根据条件使用
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * Head of the wait queue, lazily initialized.  Except for
     * initialization, it is modified only via method setHead.  Note:
     * If head exists, its waitStatus is guaranteed not to be
     * CANCELLED.
     * 
     * <p> 等待队列的头，延迟初始化。 除初始化外，只能通过setHead方法进行修改。 注意：如果head存在，则保证其waitStatus不被取消。
     */
    private transient volatile Node head;

    /**
     * Tail of the wait queue, lazily initialized.  Modified only via
     * method enq to add new wait node.
     * 
     * <p> 等待队列的尾部，延迟初始化。 仅通过方法enq进行修改以添加新的等待节点。
     */
    private transient volatile Node tail;

    /**
     * The synchronization state.
     * 
     * <p> 同步状态。
     */
    private volatile int state;

    /**
     * Returns the current value of synchronization state.
     * This operation has memory semantics of a {@code volatile} read.
     * 
     * <p> 返回同步状态的当前值。 此操作具有易失性读取的内存语义。
     * 
     * @return current state value
     * 
     * <p> 当前状态值
     */
    protected final int getState() {
        return state;
    }

    /**
     * Sets the value of synchronization state.
     * This operation has memory semantics of a {@code volatile} write.
     * 
     * <p> 设置同步状态的值。 此操作具有易失性写操作的内存语义。
     * 
     * @param newState the new state value
     * 
     * <p> 新状态值
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * Atomically sets synchronization state to the given updated
     * value if the current state value equals the expected value.
     * This operation has memory semantics of a {@code volatile} read
     * and write.
     * 
     * <p> 如果当前状态值等于期望值，则以原子方式将同步状态设置为给定的更新值。
     *  此操作具有易失性读写的内存语义。
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that the actual
     *         value was not equal to the expected value.
     *         
     * <p> 如果成功，则为true。 错误返回表示实际值不等于期望值。
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // Queuing utilities

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices
     * to improve responsiveness with very short timeouts.
     * 
     * <p> 旋转秒级比使用定时停泊更快的纳秒数。 粗略估计足以在非常短的超时时间内提高响应能力。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * Inserts node into queue, initializing if necessary. See picture above.
     * 
     * <p> 将节点插入队列，必要时进行初始化。 参见上图。
     * 
     * @param node the node to insert
     * @return node's predecessor
     * 
     * <p> 节点的前身
     */
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * Creates and enqueues node for current thread and given mode.
     * 
     * <p> 为当前线程和给定模式创建并排队节点。
     *
     * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
     * 
     * <p> Node.EXCLUSIVE用于独占，Node.SHARED用于共享
     * 
     * @return the new node
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        // 尝试enq的快速路径； 备份到完全失败
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    /**
     * Sets head of queue to be node, thus dequeuing. Called only by
     * acquire methods.  Also nulls out unused fields for sake of GC
     * and to suppress unnecessary signals and traversals.
     * 
     * <p> 将队列头设置为节点，从而出队。 仅通过acquire方法调用。 出于GC的考虑，
     * 还会清空未使用的字段，以抑制不必要的信号和遍历。
     *
     * @param node the node
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * Wakes up node's successor, if one exists.
     * 
     * <p> 唤醒节点的后继者（如果存在）。
     *
     * @param node the node
     */
    private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         * 
         * <p> 如果状态是否定的（即可能需要信号），请尝试清除以预期发出信号。 如果失败或等待线程更改状态，则可以。
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         * 
         * <p> 要取消驻留的线程保留在后继线程中，后者通常只是下一个节点。 但是，如果取消或显然为空，
         * 请从尾部向后移动以找到实际的未取消后继者。
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }

    /**
     * Release action for shared mode -- signals successor and ensures
     * propagation. (Note: For exclusive mode, release just amounts
     * to calling unparkSuccessor of head if it needs signal.)
     * 
     * <p> 共享模式下的释放动作-表示后继信号并确保传播。 （注意：对于独占模式，如果需要信号，
     * 释放仅相当于调用head的unparkSuccessor。）
     */
    private void doReleaseShared() {
        /*
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         * 
         * <p> 即使有其他正在进行的获取/发布，也要确保发布传播。 如果需要信号，则以尝试取消
         * headSuccessor的常规方式进行。 但是，如果没有，则将状态设置为PROPAGATE，
         * 以确保释放后继续传播。 此外，在执行此操作时，必须循环以防添加新节点。 另外，与
         * unparkSuccessor的其他用法不同，我们需要知道CAS重置状态是否失败，如果重新检查，则失败。
         * 
         */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     * 
     * <p> 设置队列头，并检查后继者是否可能在共享模式下等待，如果正在传播，则传播是否设置为传播> 0或PROPAGATE状态。
     *
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     * 
     * <p> tryAcquireShared的返回值
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        /*
         * <p> Try to signal next queued node if:
         *   Propagation was indicated by caller,
         *     or was recorded (as h.waitStatus either before
         *     or after setHead) by a previous operation
         *     (note: this uses sign-check of waitStatus because
         *      PROPAGATE status may transition to SIGNAL.)
         * and
         *   The next node is waiting in shared mode,
         *     or we don't know, because it appears null
         *     
         * <p> 在以下情况下尝试向下一个排队的节点发出信号：传播是由调用者指示的，还是由上一个操作（在setHead之前或之后，
         * 被记录为h.waitStatus）（注意：这使用waitStatus的符号检查），因为PROPAGATE状态可能转换为SIGNAL。 ），
         * 下一个节点正在共享模式下等待，或者我们不知道，因为它显示为空
         *
         * <p> The conservatism in both of these checks may cause
         * unnecessary wake-ups, but only when there are multiple
         * racing acquires/releases, so most need signals now or soon
         * anyway.
         * 
         * <p> 这两项检查中的保守性可能会导致不必要的唤醒，但仅当有多个赛车获取/发布时，
         * 因此无论现在还是不久，大多数人都需要发出信号。
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    // Utilities for various versions of acquire - 各种获取版本的实用程序

    /**
     * Cancels an ongoing attempt to acquire.
     * 
     * <p> 取消正在进行的获取尝试。
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        // Ignore if node doesn't exist
    	// 忽略节点是否不存在
        if (node == null)
            return;

        node.thread = null;

        // Skip cancelled predecessors
        // 跳过取消的前任
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        
        // predNext是要取消拼接的明显节点。 如果没有，以下情况将失败，在这种情况下，我们输掉了比赛，而另一个取消或发出信号，因此无需采取进一步措施。
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        
        // 可以在此处使用无条件写入代替CAS。 完成这一基本步骤后，其他节点可以跳过我们。 以前，我们不受其他线程的干扰。
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        // 如果我们是尾巴，那就移开自己。
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
        	
        	// 如果后继者需要信号，请尝试设置pred的下一个链接，以便获得一个。 否则唤醒它以传播。
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /**
     * Checks and updates status for a node that failed to acquire.
     * Returns true if thread should block. This is the main signal
     * control in all acquire loops.  Requires that pred == node.prev.
     * 
     * <p> 检查并更新无法获取的节点的状态。 如果线程应阻塞，则返回true。 这是所有采集循环中的主要信号控制。 
     * 要求pred == node.prev。
     *
     * @param pred node's predecessor holding status
     * 
     * <p> 节点的前任保持状态
     * 
     * @param node the node
     * @return {@code true} if thread should block
     * 
     * <p> 如果线程应阻塞，则为true
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             * 
             * <p> 该节点已经设置了状态，要求释放以发出信号，以便可以安全地停放。
             */
            return true;
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             * 
             * <p> 前任已取消。 跳过前任并指示重试。
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             * 
             * <p> waitStatus必须为0或PROPAGATE。 表示我们需要一个信号，但不要停放。 呼叫者将需要重试以确保在停车之前无法获取。
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * Convenience method to interrupt current thread.
     * 
     * <p> 一种方便的方法来中断当前线程。
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * Convenience method to park and then check if interrupted
     * 
     * <p> 停车的便捷方法，然后检查是否中断
     *
     * @return {@code true} if interrupted
     * 
     * <p> 如果被中断则为真
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /*
     * Various flavors of acquire, varying in exclusive/shared and
     * control modes.  Each is mostly the same, but annoyingly
     * different.  Only a little bit of factoring is possible due to
     * interactions of exception mechanics (including ensuring that we
     * cancel if tryAcquire throws exception) and other control, at
     * least not without hurting performance too much.
     * 
     * <p> 各种获取方式，包括独占/共享和控制模式。 每个都基本相同，但令人讨厌的不同。 
     * 由于异常机制（包括确保在tryAcquire抛出异常时我们取消）和其他控件的相互作用，因此只有少量分解是可能的，
     * 至少在不影响性能的前提下。
     */

    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     * 
     * <p> 以排他的不间断模式获取已在队列中的线程。 用于条件等待方法以及获取。
     *
     * @param node the node
     * @param arg the acquire argument - 获得论点
     * @return {@code true} if interrupted while waiting
     * 
     * <p> 如果在等待时被打断则为真
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive interruptible mode.
     * 
     * <p> 在排他性可中断模式下获取。
     * 
     * @param arg the acquire argument - 获得参数
     */
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive timed mode.
     * 
     * <p> 以排他定时模式进行获取。
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time - 最大等待时间
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared uninterruptible mode.
     * 
     * <p> 以共享的不间断模式进行获取。
     * 
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared interruptible mode.
     * 
     * <p> 在共享可中断模式下获取。
     * 
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared timed mode.
     * 
     * <p> 在共享定时模式下获取。
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    // Main exported methods - 主要出口方式

    /**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     * 
     * <p> 尝试以独占模式进行获取。 此方法应查询对象的状态是否允许以独占模式获取对象，如果允许则获取它。
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method {@link Lock#tryLock()}.
     * 
     * <p> 该方法始终由执行获取的线程调用。 如果此方法报告失败，则acquire方法可以将线程排队（如果尚未排队），
     * 直到其他某个线程释放释放该信号为止。 这可以用来实现方法Lock.tryLock（）。
     *
     * <p>The default
     * implementation throws {@link UnsupportedOperationException}.
     * 
     * <p> 默认实现将引发UnsupportedOperationException。
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     *        
     * <p> 获取参数。 该值始终是传递给获取方法的值，或者是在条件等待输入时保存的值。 否则该值将无法解释，并且可以代表您喜欢的任何内容。
     * 
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     *         
     * <p> 如果成功，则为true。 成功后，便已获取该对象。
     * 
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     *         
     * <p> 如果获取会使该同步器处于非法状态。 必须以一致的方式抛出此异常，以使同步正常工作。
     * 
     * @throws UnsupportedOperationException if exclusive mode is not supported
     * 
     * <p> 如果不支持独占模式
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in exclusive
     * mode.
     * 
     * <p> 尝试设置状态以反映排他模式下的发布。
     *
     * <p>This method is always invoked by the thread performing release.
     * 
     * <p> 始终由执行释放的线程调用此方法。
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     * 
     * <p> 默认实现将引发UnsupportedOperationException。
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     *        
     * <p> 释放参数。 该值始终是传递给释放方法的值，或者是输入条件等待时的当前状态值。 否则该值将无法解释，
     * 并且可以代表您喜欢的任何内容。
     * 
     * @return {@code true} if this object is now in a fully released
     *         state, so that any waiting threads may attempt to acquire;
     *         and {@code false} otherwise.
     *         
     * <p> 如果此对象现在处于完全释放状态，则所有等待线程都可以尝试获取，则返回true；否则，返回true。 否则为假。
     * 
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     *         
     * <p> 如果释放将使该同步器处于非法状态。 必须以一致的方式抛出此异常，以使同步正常工作。
     * 
     * @throws UnsupportedOperationException if exclusive mode is not supported
     * 
     * <p> 如果不支持独占模式
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to acquire in shared mode. This method should query if
     * the state of the object permits it to be acquired in the shared
     * mode, and if so to acquire it.
     * 
     * <p> 尝试以共享模式进行获取。 此方法应查询对象的状态是否允许以共享模式获取对象，如果允许则获取对象。
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread.
     * 
     * <p> 该方法始终由执行获取的线程调用。 如果此方法报告失败，则acquire方法可以将线程排队（如果尚未排队），
     * 直到其他某个线程释放释放该信号为止。
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}.
     * 
     * <p> 默认实现将引发UnsupportedOperationException。
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     *        
     * <p> 获取参数。 该值始终是传递给获取方法的值，或者是在条件等待输入时保存的值。 否则该值将无法解释，
     * 并且可以代表您喜欢的任何内容。
     * 
     * @return a negative value on failure; zero if acquisition in shared
     *         mode succeeded but no subsequent shared-mode acquire can
     *         succeed; and a positive value if acquisition in shared
     *         mode succeeded and subsequent shared-mode acquires might
     *         also succeed, in which case a subsequent waiting thread
     *         must check availability. (Support for three different
     *         return values enables this method to be used in contexts
     *         where acquires only sometimes act exclusively.)  Upon
     *         success, this object has been acquired.
     *         
     *         
     * <p> 失败的负值； 如果共享模式下的获取成功，但是没有后续共享模式下的获取可以成功，则返回零； 如果共享模式下
     * 的获取成功并且后续共享模式下的获取也可能成功，则为正值，在这种情况下，后续的等待线程必须检查可用性。 
     * （对三个不同返回值的支持使该方法可以在仅有时进行获取的情况下使用。）成功后，就已经获取了此对象。
     * 
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     *         
     * <p> 如果获取会使该同步器处于非法状态。 必须以一致的方式抛出此异常，以使同步正常工作。
     * 
     * @throws UnsupportedOperationException if shared mode is not supported
     * 
     * <p> 如果不支持共享模式
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     * 
     * <p> 尝试设置状态以反映共享模式下的发布。
     *
     * <p>This method is always invoked by the thread performing release.
     * 
     * <p> 始终由执行释放的线程调用此方法。
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     * 
     * <p> 默认实现将引发UnsupportedOperationException。
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     *        
     * <p> 释放参数。 该值始终是传递给释放方法的值，或者是输入条件等待时的当前状态值。 否则该值将无法解释，
     * 并且可以代表您喜欢的任何内容。
     * 
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     *         
     * <p> 如果此共享模式版本可以允许等待的获取（共享或独占）成功，则为true；否则为true。 否则为假
     * 
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     *         
     * <p> 如果释放将使该同步器处于非法状态。 必须以一致的方式抛出此异常，以使同步正常工作。
     * 
     * @throws UnsupportedOperationException if shared mode is not supported
     * 
     * <p> 如果不支持共享模式
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if synchronization is held exclusively with
     * respect to the current (calling) thread.  This method is invoked
     * upon each call to a non-waiting {@link ConditionObject} method.
     * (Waiting methods instead invoke {@link #release}.)
     * 
     * <p> 如果仅对当前（调用）线程进行同步，则返回true。 每次调用非等待中的ConditionObject方法时，
     * 都会调用此方法。 （等待方法改为调用发布。）
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}. This method is invoked
     * internally only within {@link ConditionObject} methods, so need
     * not be defined if conditions are not used.
     * 
     * <p> 默认实现将引发UnsupportedOperationException。 此方法仅在ConditionObject方法内部内部调用，
     * 因此，如果不使用条件，则无需定义。
     *
     * @return {@code true} if synchronization is held exclusively;
     *         {@code false} otherwise
     *         
     * <p> 如果仅以同步方式进行同步，则为true；否则为false。 否则为假
     * 
     * @throws UnsupportedOperationException if conditions are not supported
     * 
     * <p> 如果不支持条件
     * 
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * Acquires in exclusive mode, ignoring interrupts.  Implemented
     * by invoking at least once {@link #tryAcquire},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquire} until success.  This method can be used
     * to implement method {@link Lock#lock}.
     * 
     * <p> 在独占模式下获取，忽略中断。 通过至少调用一次tryAcquire来实现，并在成功后返回。 
     * 否则，线程将排队，并可能反复阻塞和解除阻塞，并调用tryAcquire直到成功。 此方法可用于实现Lock.lock方法。
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     *        
     * <p> 获取参数。 该值传送给tryAcquire，但否则不会解释，可以代表您喜欢的任何内容。
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     * 
     * <p> 以互斥方式获取，如果被中断则中止。 通过首先检查中断状态，然后至少调用一次tryAcquire来实现，
     * 并在成功后返回。 否则，线程将排队，并可能反复阻塞和解除阻塞，调用tryAcquire直到成功或线程被中断为止。 
     * 此方法可用于实现Lock.lockInterruptible方法。
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     *        
     * <p> 获取参数。 该值传送给tryAcquire，但否则不会解释，可以代表您喜欢的任何内容。
     * 
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     * 
     * <p> 尝试以互斥方式进行获取，如果被中断则中止，如果给定的超时时间过去，则失败。 通过首先检查中断状态，
     * 然后至少调用一次tryAcquire来实现，并在成功后返回。 否则，线程将排队，并可能反复阻塞和解除阻塞，
     * 调用tryAcquire直到成功或线程被中断或超时为止。 此方法可用于实现方法Lock.tryLock（long，TimeUnit）。
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     *        
     * <p> 获取参数。 该值传送给tryAcquire，但否则不会解释，可以代表您喜欢的任何内容。
     * 
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * 
     * <p> 等待的最大纳秒数
     * 
     * @return {@code true} if acquired; {@code false} if timed out
     * 
     * <p> 如果获得，则为true； 如果超时则为假
     * 
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * Releases in exclusive mode.  Implemented by unblocking one or
     * more threads if {@link #tryRelease} returns true.
     * This method can be used to implement method {@link Lock#unlock}.
     * 
     * <p> 以独占模式发布。 如果tryRelease返回true，则通过解锁一个或多个线程来实现。 
     * 此方法可用于实现Lock.unlock方法。
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryRelease} but is otherwise uninterpreted and
     *        can represent anything you like.
     *        
     *        
     * <p> 释放参数。 该值被传送到tryRelease，但否则未解释，可以代表您喜欢的任何内容。
     * 
     * @return the value returned from {@link #tryRelease}
     * 
     * <p> tryRelease返回的值
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**
     * Acquires in shared mode, ignoring interrupts.  Implemented by
     * first invoking at least once {@link #tryAcquireShared},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquireShared} until success.
     * 
     * <p> 以共享模式获取，忽略中断。 通过首先至少调用一次tryAcquireShared来实现，并在成功后返回。 
     * 否则，将线程排队，并可能反复阻塞和解除阻塞，并调用tryAcquireShared直到成功。
     * 
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     *        
     * <p> 获取参数。 该值传送给tryAcquireShared，但否则未解释，可以代表您喜欢的任何内容。
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * Acquires in shared mode, aborting if interrupted.  Implemented
     * by first checking interrupt status, then invoking at least once
     * {@link #tryAcquireShared}, returning on success.  Otherwise the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted.
     * 
     * <p> 在共享模式下获取，如果中断则中止。 通过首先检查中断状态，然后至少调用一次tryAcquireShared来实现，
     * 并在成功后返回。 否则，线程将排队，并可能反复阻塞和解除阻塞，并调用tryAcquireShared直到成功或线程被中断。
     * 
     * @param arg the acquire argument.
     * This value is conveyed to {@link #tryAcquireShared} but is
     * otherwise uninterpreted and can represent anything
     * you like.
     * 
     * <p> 获取参数。 该值传送给tryAcquireShared，但否则未解释，可以代表您喜欢的任何内容。
     * 
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     * 
     * <p> 尝试以共享模式进行获取，如果被中断则中止，如果给定的超时时间过去，则失败。 通过首先检查中断状态，
     * 然后至少调用一次tryAcquireShared来实现，并在成功后返回。 否则，线程将排队，并可能反复阻塞和解除阻塞，
     * 并调用tryAcquireShared直到成功或线程被中断或超时为止。
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     *        
     * <p> 获取参数。 该值传送给tryAcquireShared，但否则未解释，可以代表您喜欢的任何内容。
     * 
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * 
     * <p> 等待的最大纳秒数
     * 
     * @return {@code true} if acquired; {@code false} if timed out
     * 
     * <p> 如果获得，则为true； 如果超时则为假
     * 
     * @throws InterruptedException if the current thread is interrupted
     * 
     * <p> 如果当前线程被中断
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * Releases in shared mode.  Implemented by unblocking one or more
     * threads if {@link #tryReleaseShared} returns true.
     * 
     * <p> 以共享模式发布。 如果tryReleaseShared返回true，则通过解锁一个或多个线程来实现。
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryReleaseShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     *        
     * <p> 释放参数。 该值传送给tryReleaseShared，但否则未解释，可以代表您喜欢的任何内容。
     * 
     * @return the value returned from {@link #tryReleaseShared}
     * 
     * <p> 从tryReleaseShared返回的值
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // Queue inspection methods - 队列检查方法

    /**
     * Queries whether any threads are waiting to acquire. Note that
     * because cancellations due to interrupts and timeouts may occur
     * at any time, a {@code true} return does not guarantee that any
     * other thread will ever acquire.
     * 
     * <p> 查询是否有任何线程正在等待获取。 请注意，由于中断和超时导致的取消可能随时发生，
     * 因此真正的返回并不保证任何其他线程都会获取。
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     * 
     * <p> 在此实现中，此操作将以固定的时间返回。
     *
     * @return {@code true} if there may be other threads waiting to acquire
     * 
     * <p> 如果可能还有其他线程在等待获取，则为true
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * Queries whether any threads have ever contended to acquire this
     * synchronizer; that is if an acquire method has ever blocked.
     * 
     * <p> 查询是否有任何线程争夺过该同步器； 也就是说，如果获取方法曾经被阻止。
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     * 
     * <p> 在此实现中，此操作将以固定的时间返回。
     *
     * @return {@code true} if there has ever been contention
     * 
     * <p> 如果曾经存在争用则为真
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * Returns the first (longest-waiting) thread in the queue, or
     * {@code null} if no threads are currently queued.
     * 
     * <p> 返回队列中的第一个（等待时间最长）线程；如果当前没有线程在排队，则返回null。
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     * 
     * <p> 在此实现中，此操作通常以固定的时间返回，但是如果其他线程正在同时修改队列，
     * 则可能在争用时进行迭代。
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     *         
     * <p> 队列中的第一个（等待时间最长）线程；如果当前没有线程在排队，则返回null
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
    	// 仅处理快速路径，否则中继
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     * 
     * <p> 快速路径失败时调用的getFirstQueuedThread版本
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         * 
         * <p> 第一个节点通常是head.next。 尝试获取其线程字段，确保读取一致：如果线程字段被清空或s.prev不再为head，
         * 则其他一些线程在我们的某些读取之间同时执行setHead。 在遍历之前，我们尝试两次。
         * 
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         * 
         * <p> Head的下一个字段可能尚未设置，或者在setHead之后可能未设置。 因此，
         * 我们必须检查tail是否实际上是第一个节点。 如果没有，我们会继续前进，从尾巴安全地横穿到头，
         * 找到第一个，确保终止。
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * Returns true if the given thread is currently queued.
     * 
     * <p> 如果给定线程当前正在排队，则返回true。
     *
     * <p>This implementation traverses the queue to determine
     * presence of the given thread.
     * 
     * <p> 此实现遍历队列以确定给定线程的存在。
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * 
     * <p> 如果给定线程在队列中，则返回true
     * 
     * @throws NullPointerException if the thread is null
     * 
     * <p> 如果线程为空
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    /**
     * Returns {@code true} if the apparent first queued thread, if one
     * exists, is waiting in exclusive mode.  If this method returns
     * {@code true}, and the current thread is attempting to acquire in
     * shared mode (that is, this method is invoked from {@link
     * #tryAcquireShared}) then it is guaranteed that the current thread
     * is not the first queued thread.  Used only as a heuristic in
     * ReentrantReadWriteLock.
     * 
     * <p> 如果明显的第一个排队线程（如果存在）正在排他模式下等待，则返回true。 如果此方法返回true，
     * 并且当前线程正尝试以共享模式进行获取（即，从tryAcquireShared调用此方法），则可以确保当前线程不是第一个排队的线程。 
     * 仅在ReentrantReadWriteLock中用作启发式方法。
     * 
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * Queries whether any threads have been waiting to acquire longer
     * than the current thread.
     * 
     * <p> 查询是否有任何线程在等待获取比当前线程更长的时间。
     *
     * <p>An invocation of this method is equivalent to (but may be
     * more efficient than):
     * 
     * <p> 调用此方法等效于（但可能比以下方法更有效）：
     * 
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>Note that because cancellations due to interrupts and
     * timeouts may occur at any time, a {@code true} return does not
     * guarantee that some other thread will acquire before the current
     * thread.  Likewise, it is possible for another thread to win a
     * race to enqueue after this method has returned {@code false},
     * due to the queue being empty.
     * 
     * <p> 请注意，由于中断和超时引起的取消可能随时发生，因此返回true不能保证某些其他线程将在当前线程之前获取。 同样，
     * 由于队列为空，此方法返回false后，另一个线程也有可能赢得竞争。
     * 
     *
     * <p>This method is designed to be used by a fair synchronizer to
     * avoid <a href="AbstractQueuedSynchronizer#barging">barging</a>.
     * Such a synchronizer's {@link #tryAcquire} method should return
     * {@code false}, and its {@link #tryAcquireShared} method should
     * return a negative value, if this method returns {@code true}
     * (unless this is a reentrant acquire).  For example, the {@code
     * tryAcquire} method for a fair, reentrant, exclusive mode
     * synchronizer might look like this:
     * 
     * <p> 此方法设计为由公平同步器使用以避免插拔。 如果此方法返回true，则此类同步器的tryAcquire方法应返回false，
     * 而其tryAcquireShared方法应返回负值（除非这是可重入获取）。 例如，用于公平，可重入，互斥模式同步器的
     * tryAcquire方法可能如下所示：
     *
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // A reentrant acquire; increment hold count
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false;
     *   } else {
     *     // try to acquire normally
     *   }
     * }}</pre>
     *
     * @return {@code true} if there is a queued thread preceding the
     *         current thread, and {@code false} if the current thread
     *         is at the head of the queue or the queue is empty
     *         
     * <p> 如果当前线程之前有一个排队的线程，则返回true；如果当前线程位于队列的开头或队列为空，则返回false
     * 
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
    	
    	// 正确性取决于head在tail之前初始化，并且head.next是否正确（如果当前线程在队列中）。
        Node t = tail; // Read fields in reverse initialization order - 以相反的初始化顺序读取字段
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }


    // Instrumentation and monitoring methods - 仪器和监控方法

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring system state, not for synchronization
     * control.
     * 
     * <p> 返回等待获取的线程数的估计值。 该值只是一个估计值，因为在此方法遍历内部数据结构时，
     * 线程数可能会动态变化。 此方法设计用于监视系统状态，而不用于同步控制。
     * 
     *
     * @return the estimated number of threads waiting to acquire
     * 
     * <p> 等待获取的估计线程数
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     * 
     * <p> 返回一个包含可能正在等待获取的线程的集合。 因为实际的线程集在构造此结果时可能会动态变化，
     * 所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。 设计此方法是为了便于构造子类，
     * 以提供更广泛的监视功能。
     * 
     *
     * @return the collection of threads - 线程集合
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     * 
     * <p> 返回一个包含可能正在等待以独占模式获取的线程的集合。 它具有与getQueuedThreads相同的属性，
     * 除了它只返回由于互斥获取而正在等待的那些线程。
     *
     * @return the collection of threads
     * 
     * <p> 线程集合
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     * 
     * <p> 返回一个包含可能正在共享模式下等待获取的线程的集合。 它具有与getQueuedThreads相同的属性，
     * 除了它只返回由于共享获取而正在等待的那些线程。
     *
     * @return the collection of threads - 线程集合
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a string identifying this synchronizer, as well as its state.
     * The state, in brackets, includes the String {@code "State ="}
     * followed by the current value of {@link #getState}, and either
     * {@code "nonempty"} or {@code "empty"} depending on whether the
     * queue is empty.
     * 
     * <p> 返回标识此同步器及其状态的字符串。 该状态在方括号中包括字符串“ State =”，后跟getState的当前值，
     * 并取决于队列是否为空而为“ nonempty”或“ empty”。
     *
     * @return a string identifying this synchronizer, as well as its state
     * 
     * <p> 标识此同步器及其状态的字符串
     * 
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // Internal support methods for Conditions - 条件的内部支持方法

    /**
     * Returns true if a node, always one that was initially placed on
     * a condition queue, is now waiting to reacquire on sync queue.
     * 
     * <p> 如果某个节点（始终总是最初放置在条件队列中的一个节点）现在正等待在同步队列上重新获取，则返回true。
     * 
     * @param node the node
     * @return true if is reacquiring - 如果正在获取，则为true
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         * 
         * <p> node.prev可以为非null，但尚未排队，因为将CAS放入队列的CAS可能会失败。 因此，
         * 我们必须从尾部开始遍历以确保它确实做到了。 在此方法的调用中，它将始终靠近尾部，
         * 除非CAS失败（这不太可能），否则它将一直存在，因此我们几乎不会遍历太多。
         * 
         */
        return findNodeFromTail(node);
    }

    /**
     * Returns true if node is on sync queue by searching backwards from tail.
     * Called only when needed by isOnSyncQueue.
     * 
     * <p> 如果节点在同步队列中（从尾向后搜索），则返回true。 仅在isOnSyncQueue需要时调用。
     * 
     * @return true if present - 如果存在，则为true
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * 
     * <p> 将节点从条件队列转移到同步队列。 如果成功，则返回true。
     * 
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     * 
     * <p> 如果成功传输，则返回true（否则，节点在信号之前被取消）
     */
    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         * 
         * <p> 如果无法更改waitStatus，则该节点已被取消。
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         * 
         * <p> 拼接到队列上并尝试设置前任的waitStatus来指示线程（可能）正在等待。 
         * 如果取消设置或尝试设置waitStatus失败，请唤醒以重新同步（在这种情况下，
         * waitStatus可能会短暂而无害地出现错误）。
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     * 
     * <p> 在取消等待后，如有必要，传输节点以同步队列。 如果线程在发出信号之前被取消，则返回true。
     *
     * @param node the node
     * @return true if cancelled before the node was signalled
     * 
     * <p> 如果在通知节点之前取消，则为true
     */
    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        /*
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         * 
         * <p> 如果我们输给了signal（），那么直到它完成enq（）之前我们无法继续进行。 
         * 在不完整的传输过程中取消是很少见且短暂的，因此只需旋转即可。
         */
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * 
     * <p> 用当前状态值调用释放； 返回保存状态。 取消节点并在失败时引发异常。
     * 
     * @param node the condition node for this wait
     * 
     * <p> 此等待的条件节点
     * 
     * @return previous sync state - 先前的同步状态
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    // Instrumentation methods for conditions - 条件的检测方法

    /**
     * Queries whether the given ConditionObject
     * uses this synchronizer as its lock.
     * 
     * <p> 查询给定的ConditionObject是否使用此同步器作为其锁定。
     *
     * @param condition the condition
     * @return {@code true} if owned - 如果拥有，则为true
     * @throws NullPointerException if the condition is null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this synchronizer. Note that because timeouts
     * and interrupts may occur at any time, a {@code true} return
     * does not guarantee that a future {@code signal} will awaken
     * any threads.  This method is designed primarily for use in
     * monitoring of the system state.
     * 
     * <p> 查询是否有任何线程正在等待与此同步器关联的给定条件。 请注意，因为超时和中断可能随时发生，
     * 所以真正的返回并不保证将来的信号会唤醒任何线程。 此方法主要设计用于监视系统状态。
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * 
     * <p> 如果有任何等待线程，则返回true
     * 
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     *         
     * <p> 如果不保持排他同步
     * 
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     *         
     * <p> 如果给定条件与此同步器不相关
     * 
     * @throws NullPointerException if the condition is null
     * 
     * <p> 如果条件为空
     * 
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this synchronizer. Note that
     * because timeouts and interrupts may occur at any time, the
     * estimate serves only as an upper bound on the actual number of
     * waiters.  This method is designed for use in monitoring of the
     * system state, not for synchronization control.
     * 
     * <p> 返回等待与此同步器关联的给定条件的线程数的估计值。 请注意，由于超时和中断可能随时发生，因此估算值仅用作实际侍者数的上限。 
     * 此方法设计用于监视系统状态，而不用于同步控制。
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * 
     * <p> 估计的等待线程数
     * 
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     *         
     * <p> 如果不保持排他同步
     * 
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     *         
     * <p> 如果给定条件与此同步器不相关
     * 
     * @throws NullPointerException if the condition is null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this
     * synchronizer.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate. The elements of the
     * returned collection are in no particular order.
     * 
     * <p> 返回一个包含那些可能正在等待与此同步器相关的给定条件的线程的集合。
     *  因为实际的线程集在构造此结果时可能会动态变化，所以返回的集合只是尽力而为的估计。 返回的集合的元素没有特定的顺序。
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     *         
     * <p> 如果不保持排他同步
     * 
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     *         
     * <p> 如果给定条件与此同步器不相关
     * 
     * @throws NullPointerException if the condition is null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * Condition implementation for a {@link
     * AbstractQueuedSynchronizer} serving as the basis of a {@link
     * Lock} implementation.
     * 
     * <p> 用于AbstractQueuedSynchronizer的条件实现，用作Lock实现的基础。
     *
     * <p>Method documentation for this class describes mechanics,
     * not behavioral specifications from the point of view of Lock
     * and Condition users. Exported versions of this class will in
     * general need to be accompanied by documentation describing
     * condition semantics that rely on those of the associated
     * {@code AbstractQueuedSynchronizer}.
     * 
     * <p> 此类的方法文档从锁定和条件用户的角度描述了机制，而不是行为规范。 此类的导出版本通常需要随附描述条件语义的文档，
     * 这些条件语义依赖于关联的AbstractQueuedSynchronizer的语义。
     *
     * <p>This class is Serializable, but all fields are transient,
     * so deserialized conditions have no waiters.
     * 
     * <p> 此类是可序列化的，但是所有字段都是瞬态的，因此反序列化条件没有侍者。
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        /** 
         * First node of condition queue. 
         * 
         * <p> 条件队列的第一个节点。
         */
        private transient Node firstWaiter;
        /** 
         * Last node of condition queue. 
         * 
         * <p> 条件队列的最后一个节点。
         */
        private transient Node lastWaiter;

        /**
         * Creates a new {@code ConditionObject} instance.
         * 
         * <p> 创建一个新的ConditionObject实例。
         */
        public ConditionObject() { }

        // Internal methods - 内部方法

        /**
         * Adds a new waiter to wait queue.
         * 
         * <p> 添加新的服务员以等待队列。
         * 
         * @return its new wait node - 它的新的等待节点
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            // 如果lastWaiter被取消，请清除。
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * Removes and transfers nodes until hit non-cancelled one or
         * null. Split out from signal in part to encourage compilers
         * to inline the case of no waiters.
         * 
         * <p> 删除并转移节点，直到命中不可取消的一个或为null。 从信号中分离出来，部分鼓励编译器内联没有服务员的情况。
         * 
         * @param first (non-null) the first node on condition queue
         * 
         * <p> （非空）条件队列上的第一个节点
         */
        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        /**
         * Removes and transfers all nodes.
         * 
         * <p> 删除并转移所有节点。
         * 
         * @param first (non-null) the first node on condition queue
         * 
         * <p> （非空）条件队列上的第一个节点
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * Unlinks cancelled waiter nodes from condition queue.
         * Called only while holding lock. This is called when
         * cancellation occurred during condition wait, and upon
         * insertion of a new waiter when lastWaiter is seen to have
         * been cancelled. This method is needed to avoid garbage
         * retention in the absence of signals. So even though it may
         * require a full traversal, it comes into play only when
         * timeouts or cancellations occur in the absence of
         * signals. It traverses all nodes rather than stopping at a
         * particular target to unlink all pointers to garbage nodes
         * without requiring many re-traversals during cancellation
         * storms.
         * 
         * <p> 从条件队列中取消取消的服务者节点的链接。 仅在保持锁定状态下调用。 当在条件等待期间发生取消时，
         * 以及在看到lastWaiter被取消时插入新的服务员时调用此方法。 需要这种方法来避免在没有信号的情况下保留垃圾。 
         * 因此，即使可能需要完全遍历，它也只有在没有信号的情况下发生超时或取消时才起作用。 它遍历所有节点，
         * 而不是停在特定目标上，以取消所有指向垃圾节点的指针的链接，而无需在取消风暴期间进行多次遍历。
         * 
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        // public methods

        /**
         * Moves the longest-waiting thread, if one exists, from the
         * wait queue for this condition to the wait queue for the
         * owning lock.
         * 
         * <p> 将等待时间最长的线程（如果存在）从该条件的等待队列移至拥有锁的等待队列。
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         *         
         * <p> 如果isHeldExclusively返回false
         */
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        /**
         * Moves all threads from the wait queue for this condition to
         * the wait queue for the owning lock.
         * 
         * <p> 将所有线程从这种情况的等待队列移到拥有锁的等待队列。
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         *        
         * <p> 如果isHeldExclusively返回false
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * Implements uninterruptible condition wait.
         * 
         * <p> 实现不间断的条件等待。
         * 
         * <ol>
         * <li> Save lock state returned by {@link #getState}.
         * 
         * <p> 保存getState返回的锁定状态。
         * 
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         *      
         * <p> 以保存的状态作为参数调用release，如果失败则抛出IllegalMonitorStateException。
         * 
         * <li> Block until signalled.
         * 
         * <p> 阻塞直到发出信号。
         * 
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         *      
         * <p> 通过调用以保存状态作为参数的acquires的专用版本来进行reacquire。
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /*
         * For interruptible waits, we need to track whether to throw
         * InterruptedException, if interrupted while blocked on
         * condition, versus reinterrupt current thread, if
         * interrupted while blocked waiting to re-acquire.
         * 
         * <p> 对于可中断的等待，我们需要跟踪是否抛出InterruptedException（如果在有条件的情况下被阻塞而被中断），
         * 以及是否重新中断当前线程（如果在被阻塞的等待中被中断而重新获取）。
         * 
         */

        /** 
         * Mode meaning to reinterrupt on exit from wait
         * 
         * <p> 模式意味着在退出等待时重新中断
         */
        private static final int REINTERRUPT =  1;
        /** 
         * Mode meaning to throw InterruptedException on exit from wait 
         * 
         * <p> 模式的意思是在退出等待时抛出InterruptedException
         */
        private static final int THROW_IE    = -1;

        /**
         * Checks for interrupt, returning THROW_IE if interrupted
         * before signalled, REINTERRUPT if after signalled, or
         * 0 if not interrupted.
         * 
         * <p> 检查中断，如果在发出信号之前被中断，则返回THROW_IE；如果在发出信号之后，
         * 则返回REINTERRUPT；否则，则返回0。
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         * 
         * <p> 根据模式，引发InterruptedException，重新中断当前线程或不执行任何操作。
         */
        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * Implements interruptible condition wait.
         * 
         * <p> 实现可中断条件等待。
         * 
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * 
         * <p> 如果当前线程被中断，则抛出InterruptedException。
         * 
         * <li> Save lock state returned by {@link #getState}.
         * 
         * <p> 保存getState返回的锁定状态。
         * 
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         *      
         * <p> 以保存的状态作为参数调用发布，如果失败则抛出IllegalMonitorStateException。
         * 
         * <li> Block until signalled or interrupted.
         * 
         * <p> 阻塞直到发出信号或被打断。
         * 
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         *      
         * <p> 通过调用具有保存状态作为参数的acquire的专用版本来进行reacquire。
         * 
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * 
         * <p> 如果在步骤4中被阻止而被中断，则抛出InterruptedException。
         * </ol>
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * Implements timed condition wait.
         * 
         * <p> 实现定时条件等待。
         * 
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * 
         * <p> 如果当前线程被中断，则抛出InterruptedException。
         * 
         * <li> Save lock state returned by {@link #getState}.
         * 
         * <p> 保存getState返回的锁定状态。
         * 
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         *      
         * <p> 以保存的状态作为参数调用发布，如果失败则抛出IllegalMonitorStateException。
         * 
         * <li> Block until signalled, interrupted, or timed out.
         * 
         * <p> 阻塞直到发出信号，中断或超时为止。
         * 
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         *      
         * <p> 通过调用具有保存状态作为参数的acquire的专用版本来进行reacquire。
         * 
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * 
         * <p> 如果在步骤4中被阻止而被中断，则抛出InterruptedException。
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * Implements absolute timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns an estimate of the number of threads waiting on
         * this condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}.
         *
         * @return the estimated number of waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Returns a collection containing those threads that may be
         * waiting on this Condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}.
         *
         * @return the collection of threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * Setup to support compareAndSet. We need to natively implement
     * this here: For the sake of permitting future enhancements, we
     * cannot explicitly subclass AtomicInteger, which would be
     * efficient and useful otherwise. So, as the lesser of evils, we
     * natively implement using hotspot intrinsics API. And while we
     * are at it, we do the same for other CASable fields (which could
     * otherwise be done with atomic field updaters).
     * 
     * <p> 设置为支持compareAndSet。 我们需要在这里本地实现：为了允许将来进行增强，
     * 我们不能显式地继承AtomicInteger的子类，否则它将是有效的和有用的。 因此，
     * 作为罪恶之源，我们以固有方式使用热点内在API来实现。 而且，
     * 尽管我们在处理其他CASable字段，但也可以这样做（否则可以使用原子字段更新器完成）。
     * 
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS head field. Used only by enq.
     * 
     * <p> CAS头字段。 仅由enq使用。
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail field. Used only by enq.
     * 
     * <p> CAS尾场。 仅由enq使用。
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS waitStatus field of a node.
     * 
     * <p> 节点的CAS waitStatus字段。
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS next field of a node.
     * 
     * <p> 节点的CAS下一个字段。
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
