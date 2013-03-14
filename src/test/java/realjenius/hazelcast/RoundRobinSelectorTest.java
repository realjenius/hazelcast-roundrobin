package realjenius.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public class RoundRobinSelectorTest {

    @AfterMethod
    public void shutdown() { Hazelcast.shutdownAll(); }

    @Test
    public void testRotation() {
        Set<HazelcastInstance> instances = factory(3, () -> Hazelcast.newHazelcastInstance(null));
        RoundRobinSelector aSel = new RoundRobinSelector(instances.iterator().next(), "tester");
        Set<Member> selected = factory(3, () -> aSel.advance().select() );
        assert 3 == selected.size();
        assert selected.equals(transform(instances, (i) -> i.getCluster().getLocalMember()));
    }

    protected <T> Set<T> factory(int times, Supplier<T> supplier) {
        Set<T> all = new HashSet<>();
        for(int i=0; i<times; i++) {
            all.add(supplier.get());
        }
        return all;
    }

    // Current Java 8 build doesn't have transform, and guava shouldn't be needed for this. grumble. c'mon JDK 8!
    protected <T,E> Set<E> transform(Iterable<T> core, Function<T,E> xfer) {
        Set<E> conv = new HashSet<>();
        for(T t : core) {
            conv.add(xfer.apply(t));
        }
        return conv;
    }
}
