package realjenius.hazelcast;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>The round-robin selector iterates around the Hazelcast cluster, performing a best-guess rotation around the
 * cluster.</p>
 * <p>This class uses a shared atomic and consistent member ordering to perform a shared rotation around the elements
 * of the cluster.</p>
 * <p>If the cluster changes (a member joins or leaves), it will change the result of the offset operation. This may
 * result in both a member being skipped on a rotation and in a member being selected twice. For that reason this
 * should only be used for best-guess balancing, and not for hard equality/fairness.</p>
 *
 * @author realjenius/rjlorimer@electrotank.com
 */
public class RoundRobinSelector {
    private final AtomicNumber pivot;
    private final HazelcastInstance instance;

    public RoundRobinSelector(HazelcastInstance instance, String name) {
        pivot = instance.getAtomicNumber(name);
        this.instance = instance;
    }

    public RoundRobinSelector advance() {
        pivot.incrementAndGet();
        return this;
    }

    public Member select() {
        List<Member> members = new ArrayList<>(instance.getCluster().getMembers());
        int size = members.size();
        if(size == 0) throw new IllegalStateException("Cluster not connected!");
        if(size == 1) return members.get(0);
        // No streaming API in this milestone... sigh.
        // members.sort(...)
        Collections.sort(members, (left,right) -> left.getUuid().compareTo(right.getUuid()));
        return members.get((int)(pivot.get() % size));
    }

}
